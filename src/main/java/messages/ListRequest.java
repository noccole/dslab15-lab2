package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class ListRequest extends Request {
    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleListRequest(this);
    }

    @Override
    public String toString() {
        return "list";
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallListRequest(this);
    }
}
