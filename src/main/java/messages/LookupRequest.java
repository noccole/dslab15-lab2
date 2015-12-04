package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class LookupRequest extends Request {
    private String username;

    public LookupRequest() {
        super();
    }

    public LookupRequest(long messageId) {
        super(messageId);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleLookupRequest(this);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallLookupRequest(this);
    }

    @Override
    public String toString() {
        return "lookup";
    }
}
