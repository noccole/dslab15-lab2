package commands;

import states.State;
import states.StateException;
import states.StateResult;

public abstract class Response implements Message {
    private long messageId;

    public Response(Request request) {
        this.messageId = request.getMessageId();
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return new StateResult(state);
    }
}
