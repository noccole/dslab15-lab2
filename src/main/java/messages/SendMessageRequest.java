package messages;

import states.State;
import states.StateException;
import states.StateResult;

public class SendMessageRequest extends Request {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleSendMessageRequest(this);
    }

    @Override
    public String toString() {
        return "send message";
    }
}
