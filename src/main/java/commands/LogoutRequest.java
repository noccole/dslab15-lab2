package commands;

import states.State;
import states.StateException;
import states.StateResult;

public class LogoutRequest extends Request {
    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleLogoutRequest(this);
    }

    @Override
    public String toString() {
        return "logout";
    }
}
