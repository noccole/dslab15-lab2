package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

public class SendPrivateMessageResponse extends Response {
    public SendPrivateMessageResponse(SendPrivateMessageRequest request) {
        super(request);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallSendPrivateMessageResponse(this);
    }

    @Override
    public String toString() {
        return "send private message result";
    }
}
