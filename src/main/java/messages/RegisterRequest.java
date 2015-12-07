package messages;

import entities.PrivateAddress;
import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class RegisterRequest extends Request {
    private PrivateAddress privateAddress;

    public RegisterRequest() {
        super();
    }

    public RegisterRequest(long messageId) {
        super(messageId);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegisterRequest)) return false;
        if (!super.equals(o)) return false;

        RegisterRequest that = (RegisterRequest) o;

        return !(privateAddress != null ? !privateAddress.equals(that.privateAddress) : that.privateAddress != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (privateAddress != null ? privateAddress.hashCode() : 0);
        return result;
    }
}
