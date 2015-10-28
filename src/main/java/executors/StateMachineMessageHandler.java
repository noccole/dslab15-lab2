package executors;

import messages.ErrorResponse;
import messages.Message;
import states.StateException;
import states.StateMachine;

import java.util.logging.Logger;

public class StateMachineMessageHandler extends MessageHandler {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final StateMachine stateMachine;

    public StateMachineMessageHandler(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    protected Message consumeMessage(Message request) {
        LOGGER.info("StateMachineMessageHandler::consumeMessage with parameters: " + request);

        try {
            return stateMachine.handleMessage(request);
        } catch (StateException e) {
            ErrorResponse response = new ErrorResponse(request);
            response.setReason(e.toString());
            return response;
        }
    }
}
