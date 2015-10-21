package client;

import channels.Channel;
import channels.ChannelException;
import channels.MessageChannel;
import channels.TcpChannel;
import commands.SendPrivateMessageRequest;
import commands.SendPrivateMessageResponse;
import executors.*;
import states.State;
import states.StateException;
import states.StateMachine;
import states.StateResult;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PrivateMessageHandler {
    public PrivateMessageHandler(Socket socket) {
        Channel channel;
        try {
            channel = new MessageChannel(new TcpChannel(socket));
        } catch (ChannelException e) {
            e.printStackTrace();
            return;
        }

        final MessageListener listener = new ChannelMessageListener(channel);
        final MessageSender sender = new ChannelMessageSender(channel);

        final State initialState = new StateOfferService();
        final StateMachine stateMachine = new StateMachine(initialState);
        final MessageHandler handler = new StateMachineMessageHandler(stateMachine);

        new CommandBus(listener, handler, sender);
    }

    /**
     * BLOCKS!
     * @param serverSocket
     */
    public static void listen(ServerSocket serverSocket) {
        while (true) {
            try {
                final Socket clientSocket = serverSocket.accept();
                new PrivateMessageHandler(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class StateOfferService extends State {
        @Override
        public StateResult handleSendPrivateMessageRequest(SendPrivateMessageRequest request) throws StateException {
            //localBus.executeCommand(command); // TODO

            SendPrivateMessageResponse response = new SendPrivateMessageResponse(request);

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
            // TODO
        }

        @Override
        public String toString() {
            return "state shutdown private message service";
        }
    }
}
