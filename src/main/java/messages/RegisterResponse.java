package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

public class RegisterResponse extends Response {
    public RegisterResponse(RegisterRequest request) {
        super(request);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallRegisterResponse(this);
    }

    @Override
    public String toString() {
        return "register result";
    }
}
