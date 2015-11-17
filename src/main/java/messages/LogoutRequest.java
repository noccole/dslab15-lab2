package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class LogoutRequest extends Request {
    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleLogoutRequest(this);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallLogoutRequest(this);
    }

    @Override
    public String toString() {
        return "logout";
    }
}
