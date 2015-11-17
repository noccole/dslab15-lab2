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
}
