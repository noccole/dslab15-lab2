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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Response)) return false;

        Response response = (Response) o;

        return messageId == response.messageId;

    }

    @Override
    public int hashCode() {
        return (int) (messageId ^ (messageId >>> 32));
    }
}
