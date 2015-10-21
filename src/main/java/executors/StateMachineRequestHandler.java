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
            Response response = stateMachine.handleRequest(request);
            response.setRequestId(request.getRequestId());
            return response;
        } catch (StateException e) {
            ErrorResponse response = new ErrorResponse();
            response.setReason(e.toString());
            response.setRequestId(request.getRequestId());
            return response;
        }
    }
}
