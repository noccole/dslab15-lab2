package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

public class TamperedResponse extends ErrorResponse {
    public TamperedResponse(Message request) {
        super(request);
    }

    public TamperedResponse(long messageId) {
        super(messageId);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallTamperedResponse(this);
    }
}
