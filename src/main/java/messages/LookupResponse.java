package messages;

import entities.PrivateAddress;
import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

import java.util.Collection;

public class LookupResponse extends Response {
    private Collection<PrivateAddress> privateAddresses;

    public LookupResponse(LookupRequest request) {
        super(request);
    }

    public Collection<PrivateAddress> getPrivateAddresses() {
        return privateAddresses;
    }

    public void setPrivateAddresses(Collection<PrivateAddress> privateAddresses) {
        this.privateAddresses = privateAddresses;
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallLookupResponse(this);
    }

    @Override
    public String toString() {
        return "lookup result";
    }
}
