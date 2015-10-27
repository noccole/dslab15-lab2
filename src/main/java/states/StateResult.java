package states;

import messages.Message;

public class StateResult {
    private final Message result;
    private final State nextState;

    public StateResult(State nextState) {
        this.nextState = nextState;
        this.result = null;
    }

    public StateResult(State nextState, Message result) {
        this.nextState = nextState;
        this.result = result;
    }

    public Message getResult() {
        return result;
    }

    public State getNextState() {
        return nextState;
    }
}