package client;

import channels.Channel;
import channels.ChannelException;
import cli.Shell;
import commands.SendPrivateMessageRequest;
import commands.SendPrivateMessageResponse;
import executors.*;
import states.State;
import states.StateException;
import states.StateMachine;
import states.StateResult;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class PrivateMessageHandler {
    private final Channel channel;
    private final Shell shell;

    public PrivateMessageHandler(Channel channel, Shell shell, ExecutorService executorService) {
        this.channel = channel;
        this.shell = shell;

        final MessageListener listener = new ChannelMessageListener(channel);
        final MessageSender sender = new ChannelMessageSender(channel);

        final State initialState = new StateOfferService();
        final StateMachine stateMachine = new StateMachine(initialState);
        final MessageHandler handler = new StateMachineMessageHandler(stateMachine);

        final CommandBus localBus = new CommandBus();
        localBus.addMessageSender(sender);
        localBus.addMessageHandler(handler);
        localBus.addMessageListener(listener);

        executorService.submit(sender);
        executorService.submit(handler);
        executorService.submit(listener);
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
            try {
                channel.close();
            } catch (ChannelException e) {
                System.err.println("could not close channel: " + e);
            }
        }

        @Override
        public String toString() {
            return "state shutdown private message service";
        }
    }
}
