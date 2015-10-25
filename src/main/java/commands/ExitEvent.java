package commands;

import states.State;
import states.StateException;
import states.StateResult;

public class ExitEvent extends Event {
    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleExitEvent(this);
    }

    @Override
    public String toString() {
        return "exit event";
    }
}
