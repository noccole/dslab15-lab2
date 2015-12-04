package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

public class SendMessageResponse extends Response {
    public SendMessageResponse(SendMessageRequest request) {
        super(request);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallSendMessageResponse(this);
    }

    @Override
    public String toString() {
        return "send message result";
    }
}
