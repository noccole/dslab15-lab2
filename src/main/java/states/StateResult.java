package states;

import commands.Response;

public class StateResult {
    private final Response response;
    private final State nextState;

    public StateResult(State nextState) {
        this.nextState = nextState;
        this.response = null;
    }

    public StateResult(State nextState, Response response) {
        this.nextState = nextState;
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public State getNextState() {
        return nextState;
    }
}