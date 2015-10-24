package commands;

import states.State;
import states.StateException;
import states.StateResult;

public class SendPrivateMessageRequest extends Request {
    private String sender;
    private String message;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleSendPrivateMessageRequest(this);
    }

    @Override
    public String toString() {
        return "send private message";
    }
}
