package states;

import messages.*;

public abstract class State {
    public void onEntered() throws StateException {

    }

    public void onExited() throws StateException {

    }

    public StateResult handleLoginRequest(LoginRequest request) throws StateException {
        throw new StateException("request not allowed in current state");
    }

    public StateResult handleLogoutRequest(LogoutRequest request) throws StateException {
        throw new StateException("request not allowed in current state");
    }

    public StateResult handleSendMessageRequest(SendMessageRequest request) throws StateException {
        throw new StateException("request not allowed in current state");
    }

    public StateResult handleSendPrivateMessageRequest(SendPrivateMessageRequest request) throws StateException {
        throw new StateException("request not allowed in current state");
    }

    public StateResult handleRegisterRequest(RegisterRequest request) throws StateException {
        throw new StateException("request not allowed in current state");
    }

    public StateResult handleLookupRequest(LookupRequest request) throws StateException {
        throw new StateException("request not allowed in current state");
    }

    public StateResult handleListRequest(ListRequest request) throws StateException {
        throw new StateException("request not allowed in current state");
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
