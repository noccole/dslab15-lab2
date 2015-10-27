package executors;

import messages.ErrorResponse;
import messages.Message;
import states.StateException;
import states.StateMachine;

public class StateMachineMessageHandler extends MessageHandler {
    private final StateMachine stateMachine;

    public StateMachineMessageHandler(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    protected Message consumeMessage(Message request) {
        try {
            return stateMachine.handleMessage(request);
        } catch (StateException e) {
            ErrorResponse response = new ErrorResponse(request);
            response.setReason(e.toString());
            return response;
        }
    }
}
