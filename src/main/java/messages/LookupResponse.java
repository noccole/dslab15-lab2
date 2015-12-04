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
}
