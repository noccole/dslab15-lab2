package states;

import messages.*;
import util.Utf8;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

public abstract class State {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    public void onEntered() throws StateException {

    }

    public void onExited() throws StateException {

    }

    public StateResult handleUnknownRequest(UnknownRequest request) throws StateException {
        final ErrorResponse response = new ErrorResponse(request);
        response.setReason("Unknown request: " + request.getReason());

        return new StateResult(this, response);
    }

    public StateResult handleTamperedRequest(TamperedRequest request) throws StateException {
        final TamperedResponse response = new TamperedResponse(request);

        final Message originalRequest = request.getRequest();
        originalRequest.applyTo(new State() { // get the right tampered message for each request
            @Override
            public StateResult handleUnknownRequest(UnknownRequest request) throws StateException {
                try {
                    LOGGER.warning("Tampered unknown request: " + Utf8.encodeByteArray(request.getRequestData()));
                    response.setReason(Utf8.encodeByteArray(request.getRequestData()));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.warning("Could not UTF-8 encode request data");
                }
                return null;
            }

            @Override
            public StateResult handleLoginRequest(LoginRequest request) throws StateException {
                LOGGER.warning("Tampered login request: " + request.getUsername() + ", " + request.getPassword());
                response.setReason("Tampered login request: " + request.getUsername() + ", " + request.getPassword());
                return null;
            }

            @Override
            public StateResult handleLogoutRequest(LogoutRequest request) throws StateException {
                LOGGER.warning("Tampered logout request");
                response.setReason("Tampered logout request");
                return null;
            }

            @Override
            public StateResult handleSendMessageRequest(SendMessageRequest request) throws StateException {
                LOGGER.warning("Tampered send message request: " + request.getMessage());
                response.setReason(request.getMessage()); // see stage3 requirements
                return null;
            }

            @Override
            public StateResult handleSendPrivateMessageRequest(SendPrivateMessageRequest request) throws StateException {
                LOGGER.warning("Tampered send private message request: " + request.getSender() + ", " + request.getMessage());
                response.setReason(request.getMessage()); // see stage3 requirements
                return null;
            }

            @Override
            public StateResult handleRegisterRequest(RegisterRequest request) throws StateException {
                LOGGER.warning("Tampered register request: " + request.getPrivateAddress());
                response.setReason("Tampered register request: " + request.getPrivateAddress());
                return null;
            }

            @Override
            public StateResult handleLookupRequest(LookupRequest request) throws StateException {
                LOGGER.warning("Tampered lookup request: " + request.getUsername());
                response.setReason("Tampered lookup request: " + request.getUsername());
                return null;
            }

            @Override
            public StateResult handleListRequest(ListRequest request) throws StateException {
                LOGGER.warning("Tampered list request");
                response.setReason("Tampered list request");
                return null;
            }

            @Override
            public StateResult handleMessageEvent(MessageEvent event) throws StateException {
                LOGGER.warning("Tampered message event: " + event.getMessage());
                response.setReason("Tampered message event: " + event.getMessage());
                return null;
            }

            @Override
            public StateResult handleExitEvent(ExitEvent event) throws StateException {
                LOGGER.warning("Tampered exit event");
                response.setReason("Tampered exit event");
                return null;
            }

            @Override
            public StateResult handleUserStateChangedEvent(UserPresenceChangedEvent event) throws StateException {
                LOGGER.warning("Tampered user state changed event: " + event.getUsername() + ", " + event.getPresence());
                response.setReason("Tampered user state changed event: " + event.getUsername() + ", " + event.getPresence());
                return null;
            }

            @Override
            public StateResult handleTamperedRequest(TamperedRequest request) throws StateException {
                LOGGER.warning("Tampered 'tampered' request ... dafuq?! This should be unreachable!");
                assert false; // should be unreachable
                return null;
            }
        });

        return new StateResult(this, response);
    }

    public StateResult handleLoginRequest(LoginRequest request) throws StateException {
        throw new StateException("login is not allowed in current state");
    }

    public StateResult handleLogoutRequest(LogoutRequest request) throws StateException {
        throw new StateException("logout is not allowed in current state");
    }

    public StateResult handleSendMessageRequest(SendMessageRequest request) throws StateException {
        throw new StateException("send message is not allowed in current state");
    }

    public StateResult handleSendPrivateMessageRequest(SendPrivateMessageRequest request) throws StateException {
        throw new StateException("send private message is not allowed in current state");
    }

    public StateResult handleRegisterRequest(RegisterRequest request) throws StateException {
        throw new StateException("register is not allowed in current state");
    }

    public StateResult handleLookupRequest(LookupRequest request) throws StateException {
        throw new StateException("lookup is not allowed in current state");
    }

    public StateResult handleListRequest(ListRequest request) throws StateException {
        throw new StateException("list is not allowed in current state");
    }

    public StateResult handleMessageEvent(MessageEvent event) throws StateException {
        return new StateResult(this);
    }

    public StateResult handleExitEvent(ExitEvent event) throws StateException {
        return new StateResult(this);
    }

    public StateResult handleUserStateChangedEvent(UserPresenceChangedEvent event) throws StateException {
        return new StateResult(this);
    }
}
