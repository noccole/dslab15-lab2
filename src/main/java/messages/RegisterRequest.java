package messages;

import entities.PrivateAddress;
import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class RegisterRequest extends Request {
    private PrivateAddress privateAddress;

    public PrivateAddress getPrivateAddress() {
        return privateAddress;
    }

    public void setPrivateAddress(PrivateAddress privateAddress) {
        this.privateAddress = privateAddress;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleRegisterRequest(this);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallRegisterRequest(this);
    }

    @Override
    public String toString() {
        return "register";
    }
}
