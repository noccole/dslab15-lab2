package executors;

import commands.ErrorResponse;
import commands.Message;
import states.StateException;
import states.StateMachine;

public class StateMachineMessageHandler extends MessageHandler {
    private StateMachine stateMachine;

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
