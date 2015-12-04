package messages;

import states.State;
import states.StateException;
import states.StateResult;

public abstract class Response implements Message {
    private final long messageId;

    public Response(Message request) {
        this.messageId = request.getMessageId();
    }

    public Response(long messageId) { this.messageId = messageId; }

    @Override
    public long getMessageId() {
        return messageId;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return new StateResult(state);
    }
}
