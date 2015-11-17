package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

public class LogoutResponse extends Response {
    public LogoutResponse(LogoutRequest request) {
        super(request);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallLogoutResponse(this);
    }

    @Override
    public String toString() {
        return "logout result";
    }
}
