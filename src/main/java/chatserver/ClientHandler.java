package chatserver;

import channels.Channel;
import channels.ChannelException;
import entities.User;
import executors.EventDistributor;
import messages.*;
import service.UserService;
import shared.HandlerBase;
import states.State;
import states.StateException;
import states.StateResult;

import java.util.concurrent.ExecutorService;

class ClientHandler extends HandlerBase {
    private final Channel channel;
    private final UserService userService;
    private final EventDistributor eventDistributor;

    public ClientHandler(Channel channel, UserService userService,
                         EventDistributor eventDistributor, ExecutorService executorService) {
        this.channel = channel;
        this.userService = userService;
        this.eventDistributor = eventDistributor;

        init(channel, executorService, new StateOffline());
    }

    private class StateOffline extends State {
        @Override
        public StateResult handleLoginRequest(LoginRequest request) throws StateException {
            boolean success = false;

            final User user = userService.find(request.getUsername());
            if (user != null && user.getPresence() == User.Presence.Offline) {
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
        public StateResult handleExitEvent(ExitEvent event) throws StateException {
            try {
                channel.close();
            } catch (ChannelException e) {
                System.err.println("could not close channel");
            }

            return new StateResult(this);
        }
    }

    private class StateOnline extends State {
        private final User user;
        private Channel.EventHandler channelEventHandler;

        public StateOnline(User user) {
            this.user = user;
        }

        @Override
        public void onEntered() throws StateException {
            channelEventHandler = new Channel.EventHandler() {
                @Override
                public void onChannelClosed() {
                    userService.logout(user);
                }
            };
            channel.addEventHandler(channelEventHandler);

            eventDistributor.subscribe(getSender());
        }

        @Override
        public void onExited() throws StateException {
            eventDistributor.unsubscribe(getSender());
            channel.removeEventHandler(channelEventHandler);
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
            user.addPrivateAddress(request.getPrivateAddress());

            final RegisterResponse response = new RegisterResponse(request);

            return new StateResult(this, response);
        }

        @Override
        public StateResult handleLookupRequest(LookupRequest request) throws StateException {
            final User requestedUser = userService.find(request.getUsername());

            if (requestedUser != null) {
                final LookupResponse response = new LookupResponse(request);
                response.setPrivateAddresses(requestedUser.getPrivateAddresses());

                return new StateResult(this, response);
            } else {
                final ErrorResponse response = new ErrorResponse(request);
                response.setReason("user '" + request.getUsername() + "' not found");
                return new StateResult(this, response);
            }
        }

        @Override
        public StateResult handleExitEvent(ExitEvent event) throws StateException {
            userService.logout(user);
            stop();
            return new StateResult(new StateOffline());
        }
    }
}
