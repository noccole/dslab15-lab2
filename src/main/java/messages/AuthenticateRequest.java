package messages;

import states.State;
import states.StateException;
import states.StateResult;

public class AuthenticateRequest extends Request {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleAuthenticateRequest(this);
    }

    @Override
    public String toString() {
        return "authenticate";
    }
}
