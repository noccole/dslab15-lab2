package commands;

import states.State;
import states.StateException;

public class LookupRequest extends Request {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public State.StateResult applyTo(State state) throws StateException {
        return state.handleLookupRequest(this);
    }

    @Override
    public String toString() {
        return "lookup";
    }
}
