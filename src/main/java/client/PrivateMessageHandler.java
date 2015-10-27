package client;

import channels.Channel;
import channels.ChannelException;
import channels.Packet;
import cli.Shell;
import commands.Message;
import commands.SendPrivateMessageRequest;
import commands.SendPrivateMessageResponse;
import executors.*;
import states.State;
import states.StateException;
import states.StateMachine;
import states.StateResult;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

class PrivateMessageHandler {
    private final Channel channel;
    private final Shell shell;

    private final MessageListener listener;
    private final MessageSender sender;
    private final MessageHandler handler;

    public PrivateMessageHandler(Channel channel, Shell shell, ExecutorService executorService) {
        this.channel = channel;
        this.shell = shell;

        listener = new ChannelMessageListener(channel);
        sender = new ChannelMessageSender(channel);

        final State initialState = new StateOfferService();
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

    public void stop() {
        try {
            channel.close();
        } catch (ChannelException e) {
            System.err.println("could not close channel: " + e);
        }

        listener.cancel(true);
        handler.cancel(true);
        sender.cancel(true);
    }

    private class StateOfferService extends State {
        @Override
        public StateResult handleSendPrivateMessageRequest(SendPrivateMessageRequest request) throws StateException {
            try {
                shell.writeLine("[PRV] " + request.getSender() + ": " + request.getMessage());
            } catch (IOException e) {
                System.err.println("could not print private message: " + e);
            }

            final SendPrivateMessageResponse response = new SendPrivateMessageResponse(request);

            return new StateResult(new StateShutdownService(), response);
        }

        @Override
        public String toString() {
            return "state offer private message service";
        }
    }

    private class StateShutdownService extends State {
        @Override
        public void onEntered() throws StateException {
            stop();
        }

        @Override
        public String toString() {
            return "state shutdown private message service";
        }
    }
}
