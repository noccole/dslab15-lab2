package shared;

import channels.Channel;
import channels.ChannelException;
import channels.Packet;
import messages.Message;
import states.State;
import states.StateMachine;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public abstract class HandlerBase {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private Channel channel;
    private HandlerManager handlerManager;

    private MessageListener listener;
    private MessageHandler handler;
    private MessageSender sender;

    protected void init(Channel channel, ExecutorService executorService, HandlerManager handlerManager, State initialState) {
        this.channel = channel;
        this.handlerManager = handlerManager;

        listener = new ChannelMessageListener(channel);
        sender = new ChannelMessageSender(channel);

        final StateMachine stateMachine = new StateMachine(initialState);
        handler = new StateMachineMessageHandler(stateMachine);

        listener.addEventHandler(new MessageListener.EventHandler() {
            @Override
            public void onMessageReceived(Packet<Message> message) {
                try {
                    handler.handleMessage(message);
                } catch (TaskCancelledException e) {
                    LOGGER.warning("Handler '" + handler + "' was cancelled");
                }
            }

            @Override
            public void onCancelled() {

            }
        });
        handler.addEventHandler(new MessageHandler.EventHandler() {
            @Override
            public void onMessageHandled(Packet<Message> message, Packet<Message> result) {
                try {
                    sender.sendMessage(result);
                } catch (TaskCancelledException e) {
                    LOGGER.warning("Sender '" + sender + "' was cancelled");
                }
            }

            @Override
            public void onCancelled() {

            }
        });

        executorService.submit(sender);
        executorService.submit(handler);
        executorService.submit(listener);

        handlerManager.registerHandler(this);

        channel.addEventHandler(new Channel.EventHandler() {
            @Override
            public void onChannelClosed() {
                stop();
            }
        });
    }

    protected MessageListener getListener() {
        return listener;
    }

    protected MessageHandler getHandler() {
        return handler;
    }

    protected MessageSender getSender() {
        return sender;
    }

    public void stop() {
        handlerManager.unregisterHandler(this);

        listener.cancel();
        handler.cancel();
        sender.cancel();

        try {
            channel.close();
        } catch (ChannelException e) {
            LOGGER.warning("could not close channel: " + e);
        }
    }
}
