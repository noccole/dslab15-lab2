package commands;

import states.State;
import states.StateException;
import states.StateResult;

public class MessageEvent extends Event {
    private String username;
    private String message;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleMessageEvent(this);
    }

    @Override
    public String toString() {
        return "message event";
    }
}
