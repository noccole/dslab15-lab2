package chatserver;

import channels.ChannelException;
import channels.CommandChannel;
import channels.Channel;
import channels.TcpChannel;
import commands.*;
import executors.*;
import executors.RemoteCommandHandler;
import states.State;
import states.StateException;
import states.StateMachine;

import java.net.Socket;

public class ClientHandler {
    private final UserService userService;

    public ClientHandler(Socket socket, UserService userService) {
        this.userService = userService;

        Channel channel;
        try {
            channel = new CommandChannel(new TcpChannel(socket));
        } catch (ChannelException e) {
            e.printStackTrace();
            return;
        }

        final RequestListener listener = new ChannelRequestListener(channel);
        final ResponseSender sender = new ChannelResponseSender(channel);

        final State initialState = new StateOffline();
        final StateMachine stateMachine = new StateMachine(initialState);
        final RequestHandler handler = new StateMachineRequestHandler(stateMachine);

        new CommandBus(listener, handler, sender);
    }

    private class StateOffline extends State {
        @Override
        public State handleLoginRequest(LoginRequest command) throws StateException {
            boolean success = false;

            final User user = userService.find(command.getUsername());
            if (user != null) {
                success = userService.login(user, command.getPassword());
            }

            // inform the client
            final LoginResponse result = new LoginResponse();
            result.setSuccess(success);
            // TODO

            if (success) {
                return new StateOnline(user);
            } else {
                return this;
            }
        }

        @Override
        public String toString() {
            return "client state offline";
        }
    }

    private class StateOnline extends State {
        private final User user;

        public StateOnline(User user) {
            this.user = user;
        }

        @Override
        public void onEntered() throws StateException {
            serverBus.addCommandExecutor(clientSideExecutor);
        }

        @Override
        public void onExited() throws StateException {
            serverBus.removeCommandExecutor(clientSideExecutor);
        }

        @Override
        public State handleLogoutRequest(LogoutRequest command) throws StateException {
            userService.logout(user);

            // inform the client
            final LogoutResponse result = new LogoutResponse();
            clientSideExecutor.executeCommand(result);

            return new StateOffline();
        }

        @Override
        public State handleSendMessageRequest(SendMessageRequest command) throws StateException {
            command.setUsername(user.getUsername()); // override the user name to avoid identity spoofing
            serverBus.executeCommand(command); // forward command to server bus
            return this;
        }

        @Override
        public State handleRegisterRequest(RegisterRequest command) throws StateException {
            user.setPrivateAddress(command.getAddress());
            return this;
        }

        @Override
        public State handleLookupRequest(LookupRequest command) throws StateException {
            final User other = userService.find(command.getUsername());
            if (other != null) {
                // TODO send response
            }
            return this;
        }

        @Override
        public String toString() {
            return "client state online";
        }
    }
}
