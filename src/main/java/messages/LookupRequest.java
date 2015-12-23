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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LookupRequest)) return false;
        if (!super.equals(o)) return false;

        LookupRequest that = (LookupRequest) o;

        return !(username != null ? !username.equals(that.username) : that.username != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (username != null ? username.hashCode() : 0);
        return result;
    }
}
