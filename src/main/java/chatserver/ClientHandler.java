package chatserver;

import channels.Channel;
import commands.*;
import entities.User;
import executors.*;
import service.UserService;
import states.State;
import states.StateException;
import states.StateMachine;
import states.StateResult;

public class ClientHandler {
    private final UserService userService;
    private final EventDistributor eventDistributor;
    private MessageSender sender = null;

    public ClientHandler(Channel channel, UserService userService, EventDistributor eventDistributor) {
        this.userService = userService;
        this.eventDistributor = eventDistributor;

        final MessageListener listener = new ChannelMessageListener(channel);
        sender = new ChannelMessageSender(channel);

        final State initialState = new StateOffline();
        final StateMachine stateMachine = new StateMachine(initialState);
        final MessageHandler handler = new StateMachineMessageHandler(stateMachine);

        final CommandBus localBus = new CommandBus();
        localBus.addMessageSender(sender);
        localBus.addMessageHandler(handler);
        localBus.addMessageListener(listener);
    }

    private class StateOffline extends State {
        @Override
        public StateResult handleLoginRequest(LoginRequest request) throws StateException {
            boolean success = false;

            final User user = userService.find(request.getUsername());
            if (user != null) {
                success = userService.login(user, request.getPassword());
            }

            final LoginResponse response = new LoginResponse(request);
            response.setSuccess(success);

            State nextState;
            if (success) {
                nextState = new StateOnline(user);
            } else {
                nextState = this;
            }

            return new StateResult(nextState, response);
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
            eventDistributor.subscribe(sender);
        }

        @Override
        public void onExited() throws StateException {
            eventDistributor.unsubscribe(sender);
        }

        @Override
        public StateResult handleLogoutRequest(LogoutRequest request) throws StateException {
            userService.logout(user);

            final LogoutResponse response = new LogoutResponse(request);

            return new StateResult(new StateOffline(), response);
        }

        @Override
        public StateResult handleSendMessageRequest(SendMessageRequest request) throws StateException {
            final MessageEvent event = new MessageEvent();
            event.setUsername(user.getUsername());
            event.setMessage(request.getMessage());
            eventDistributor.publish(event);

            final SendMessageResponse response = new SendMessageResponse(request);

            return new StateResult(this, response);
        }

        @Override
        public StateResult handleRegisterRequest(RegisterRequest request) throws StateException {
            user.setPrivateAddress(request.getAddress());

            final RegisterResponse response = new RegisterResponse(request);

            return new StateResult(this, response);
        }

        @Override
        public StateResult handleLookupRequest(LookupRequest request) throws StateException {
            final User requestedUser = userService.find(request.getUsername());

            final LookupResponse response = new LookupResponse(request);
            if (requestedUser != null) {
                response.setPrivateAddress(requestedUser.getPrivateAddress());
            }

            return new StateResult(this, response);
        }

        @Override
        public String toString() {
            return "client state online";
        }
    }
}
