package executors;

import commands.ErrorResponse;
import commands.Request;
import commands.Response;
import states.StateException;
import states.StateMachine;

public class StateMachineRequestHandler extends RequestHandler {
    private StateMachine stateMachine;

    public StateMachineRequestHandler(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    protected Response consumeRequest(Request request) {
        try {
            return stateMachine.handleRequest(request);
        } catch (StateException e) {
            ErrorResponse response = new ErrorResponse(request);
            response.setReason(e.toString());
            return response;
        }
    }
}
