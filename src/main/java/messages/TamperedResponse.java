package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

public class TamperedResponse extends ErrorResponse {
    public TamperedResponse(Message request) {
        super(request);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallTamperedResponse(this);
    }
}
