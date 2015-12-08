package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class TamperedRequest extends Request {
    private final Message request;

    public TamperedRequest(Message request) {
        super(request.getMessageId());
        this.request = request;
    }

    public Message getRequest() {
        return request;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleTamperedRequest(this);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallTamperedRequest(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TamperedRequest)) return false;
        if (!super.equals(o)) return false;

        TamperedRequest that = (TamperedRequest) o;

        return !(request != null ? !request.equals(that.request) : that.request != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (request != null ? request.hashCode() : 0);
        return result;
    }
}
