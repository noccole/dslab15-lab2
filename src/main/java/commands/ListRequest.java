package commands;

import states.State;
import states.StateException;
import states.StateResult;

public class ListRequest extends Request {
    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleListRequest(this);
    }

    @Override
    public String toString() {
        return "list";
    }
}
