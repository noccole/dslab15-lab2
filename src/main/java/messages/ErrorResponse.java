package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

public class ErrorResponse extends Response {
    private String reason;

    public ErrorResponse(Message request) {
        super(request);
    }

    public ErrorResponse(long messageId) {
        super(messageId);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallErrorResponse(this);
    }
}
