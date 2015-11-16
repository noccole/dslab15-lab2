package shared;

import messages.*;
import states.State;
import states.StateException;
import states.StateResult;
import util.Utf8;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

public class TamperedResponseBuilder extends State {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    public static TamperedResponse getResponseFor(TamperedRequest request) {
        final TamperedResponse response = new TamperedResponse(request);

        final Message originalRequest = request.getRequest();
        try {
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
        } catch (StateException e) {
            LOGGER.warning("Could not handle tampered message!");
        }

        return response;
    }


}
