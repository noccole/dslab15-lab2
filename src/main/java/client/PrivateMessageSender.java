package client;

import channels.Channel;
import channels.ChannelException;
import channels.MessageChannel;
import channels.TcpChannel;
import commands.LookupRequest;
import commands.LookupResponse;
import commands.SendPrivateMessageRequest;
import commands.SendPrivateMessageResponse;
import executors.*;
import states.State;
import states.StateException;
import states.StateMachine;

public class PrivateMessageSender {
    private CommandBus clientBus;
    private CommandBus localBus;

    private MessageHandler localExecutor;
    private MessageHandler clientExecutor;

    public PrivateMessageSender(CommandBus clientBus) {
        this.clientBus = clientBus;
        this.localBus = new CommandBus();

        // remote executor/listener
        Channel commandChannel;
        try {
            commandChannel = new MessageChannel(new TcpChannel(socket));
        } catch (ChannelException e) {
            e.printStackTrace();
            return;
        }
        this.clientExecutor = new RemoteCommandHandler(commandChannel);
        final MessageListener clientListener = new ChannelMessageListener(commandChannel);

        // local executor
        final State initialState = new StateLookupPrivateAddress();
        final StateMachine stateMachine = new StateMachine(initialState);
        this.localExecutor = new StateMachineMessageHandler(stateMachine);

        localBus.addCommandExecutor(localExecutor);
        localBus.addCommandListener(clientListener);
    }

    private class StateLookupPrivateAddress extends State {
        @Override
        public void onEntered() throws StateException {
            // listen for lookup result
            clientBus.addCommandExecutor(localExecutor); // FIXME maybe bus.subscribe(this.getStateMachine())

            LookupRequest command = new LookupRequest();
            command.setUsername("user");
            clientBus.executeCommand(command);
        }

        @Override
        public void onExited() throws StateException {
            clientBus.removeCommandExecutor(localExecutor); // FIXME maybe bus.unsubscribe(this.getStateMachine())
        }

        @Override
        public State applyLookupResult(LookupResponse result) throws StateException {
            if (result != null) { // TODO add real check
                return new StateSendPrivateMessage();
            } else {
                return new StateShutdown();
            }
        }

        @Override
        public String toString() {
            return "state lookup private address";
        }
    }

    private class StateSendPrivateMessage extends State {
        @Override
        public void onEntered() throws StateException {
            SendPrivateMessageRequest command = new SendPrivateMessageRequest();
            command.setReceiver(""); // TODO
            command.setMessage("");
            clientExecutor.executeCommand(command);
        }

        @Override
        public State applySendPrivateMessageResult(SendPrivateMessageResponse result) throws StateException {
            return new StateShutdown();
        }

        @Override
        public String toString() {
            return "state send private message";
        }
    }

    private class StateShutdown extends State {
        @Override
        public void onEntered() throws StateException {
            localBus.stop();
        }

        @Override
        public String toString() {
            return "state shutdown private message sender";
        }
    }
}
