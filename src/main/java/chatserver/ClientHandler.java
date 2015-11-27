package chatserver;

import channels.Channel;
import entities.PrivateAddress;
import entities.User;
import messages.*;
import service.ServiceException;
import service.UserService;
import shared.EventDistributor;
import shared.HandlerBase;
import shared.HandlerManager;
import shared.MessageSender;
import states.State;
import states.StateException;
import states.StateResult;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

class ClientHandler extends HandlerBase {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final Channel channel;
    private final UserService userService;
    private final EventDistributor eventDistributor;

    public ClientHandler(Channel channel, UserService userService, EventDistributor eventDistributor,
                         ExecutorService executorService, HandlerManager handlerManager) {
        this.channel = channel;
        this.userService = userService;
        this.eventDistributor = eventDistributor;

        init(channel, executorService, handlerManager, new StateOffline());
    }

    private class StateOffline extends State {
        @Override
        public StateResult handleLoginRequest(LoginRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOffline::handleLoginRequest with parameters: " + request);

            LoginResponse.ResponseCode responseCode;

            final User user = userService.find(request.getUsername());
            if (user != null) {
                if (user.getPresence() == User.Presence.Offline) {
                    if (userService.login(user, request.getPassword())) {
                        responseCode = LoginResponse.ResponseCode.Success;
                    } else {
                        responseCode = LoginResponse.ResponseCode.WrongPassword;
                    }
                } else {
                    responseCode = LoginResponse.ResponseCode.UserAlreadyLoggedIn;
                }
            } else {
                responseCode = LoginResponse.ResponseCode.UnknownUser;
            }

            final LoginResponse response = new LoginResponse(request);
            response.setResponse(responseCode);

            final State nextState;
            if (responseCode == LoginResponse.ResponseCode.Success) {
                nextState = new StateOnline(user);
            } else {
                nextState = this;
            }

            return new StateResult(nextState, response);
        }

        @Override
        public StateResult handleExitEvent(ExitEvent event) throws StateException {
            LOGGER.info("ClientHandler::StateOffline::handleExitEvent with parameters: " + event);

            return new StateResult(new StateExit());
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
            LOGGER.info("ClientHandler::StateOnline::onEntered");

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
            LOGGER.info("ClientHandler::StateOnline::onExited");

            eventDistributor.unsubscribe(getSender());
            channel.removeEventHandler(channelEventHandler);

            userService.logout(user); // guarantee that the user is logged out when we leave this state
        }

        @Override
        public StateResult handleLogoutRequest(LogoutRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleLogoutRequest with parameters: " + request);

            userService.logout(user);

            final LogoutResponse response = new LogoutResponse(request);

            return new StateResult(new StateOffline(), response);
        }

        @Override
        public StateResult handleSendMessageRequest(SendMessageRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleSendMessageRequest with parameters: " + request);

            final MessageEvent event = new MessageEvent();
            event.setUsername(user.getUsername());
            event.setMessage(request.getMessage());
            eventDistributor.publish(event, new HashSet<MessageSender>() {{
                add(getSender()); // don't forward this event to our sender
            }});

            final SendMessageResponse response = new SendMessageResponse(request);

            return new StateResult(this, response);
        }

        @Override
        public StateResult handleRegisterRequest(RegisterRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleRegisterRequest with parameters: " + request);

            try {
                userService.registerPrivateAddress(user, request.getPrivateAddress());
                final RegisterResponse response = new RegisterResponse(request);
                return new StateResult(this, response);
            } catch (ServiceException e) {
                final ErrorResponse response = new ErrorResponse(request);
                response.setReason(e.getMessage());
                return new StateResult(this, response);
            }
        }

        @Override
        public StateResult handleLookupRequest(LookupRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleLookupRequest with parameters: " + request);

            final User requestedUser = userService.find(request.getUsername());

            if (requestedUser != null) {
                final PrivateAddress privateAddress;
                try {
                    privateAddress = userService.lookupPrivateAddress(requestedUser);
                } catch (ServiceException e) {
                    final ErrorResponse response = new ErrorResponse(request);
                    response.setReason(e.getMessage());
                    return new StateResult(this, response);
                }

                final LookupResponse response = new LookupResponse(request);
                response.setPrivateAddress(privateAddress);
                return new StateResult(this, response);
            } else {
                final ErrorResponse response = new ErrorResponse(request);
                response.setReason("user '" + request.getUsername() + "' not found");
                return new StateResult(this, response);
            }
        }

        @Override
        public StateResult handleExitEvent(ExitEvent event) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleExitEvent with parameters: " + event);

            return new StateResult(new StateExit());
        }
    }

    private class StateExit extends State {
        @Override
        public void onEntered() throws StateException {
            LOGGER.info("ClientHandler::StateExit::onEntered");

            stop();
        }
    }
}
