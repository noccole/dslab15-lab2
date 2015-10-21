package commands;

import states.State;
import states.StateException;

public class ListRequest extends Request {
    @Override
    public State.StateResult applyTo(State state) throws StateException {
        return state.handleListRequest(this);
    }

    @Override
    public String toString() {
        return "list";
    }
}
