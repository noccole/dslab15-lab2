package shared;

import channels.Channel;
import channels.ChannelException;
import channels.Packet;
import executors.*;
import messages.Message;
import states.State;
import states.StateMachine;

import java.util.concurrent.ExecutorService;

public abstract class HandlerBase {
    private Channel channel;

    private MessageListener listener;
    private MessageHandler handler;
    private MessageSender sender;

    protected void init(Channel channel, ExecutorService executorService, State initialState) {
        this.channel = channel;

        listener = new ChannelMessageListener(channel);
        sender = new ChannelMessageSender(channel);

        final StateMachine stateMachine = new StateMachine(initialState);
        handler = new StateMachineMessageHandler(stateMachine);

        listener.addEventHandler(new MessageListener.EventHandler() {
            @Override
            public void onMessageReceived(Packet<Message> message) {
                handler.handleMessage(message);
            }
        });
        handler.addEventHandler(new MessageHandler.EventHandler() {
            @Override
            public void onMessageHandled(Packet<Message> message, Packet<Message> result) {
                sender.sendMessage(result);
            }
        });

        executorService.submit(sender);
        executorService.submit(handler);
        executorService.submit(listener);
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
        listener.cancel(true);
        handler.cancel(true);

        // wait until sender queue is empty
        sender.waitForAllMessagesSend();
        sender.cancel(true);

        try {
            channel.close();
        } catch (ChannelException e) {
            System.err.println("could not close channel: " + e);
        }
    }
}
