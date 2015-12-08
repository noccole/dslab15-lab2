package messages;

import entities.PrivateAddress;
import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

import java.util.Collection;

public class LookupResponse extends Response {
    private PrivateAddress privateAddress;

    public LookupResponse(LookupRequest request) {
        super(request);
    }

    public LookupResponse(long messageId) {
        super(messageId);
    }

    public PrivateAddress getPrivateAddress() {
        return privateAddress;
    }

    public void setPrivateAddress(PrivateAddress privateAddress) {
        this.privateAddress = privateAddress;
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallLookupResponse(this);
    }

    @Override
    public String toString() {
        return "lookup result";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LookupResponse)) return false;
        if (!super.equals(o)) return false;

        LookupResponse that = (LookupResponse) o;

        return !(privateAddress != null ? !privateAddress.equals(that.privateAddress) : that.privateAddress != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (privateAddress != null ? privateAddress.hashCode() : 0);
        return result;
    }
}
