package states;

import messages.*;
import shared.TamperedResponseBuilder;

public abstract class State {
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
        final TamperedResponse response = TamperedResponseBuilder.getResponseFor(request);

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
