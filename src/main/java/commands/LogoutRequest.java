package commands;

import states.State;
import states.StateException;

public class LogoutRequest extends Request {
    @Override
    public State.StateResult applyTo(State state) throws StateException {
        return state.handleLogoutRequest(this);
    }

    @Override
    public String toString() {
        return "logout";
    }
}
