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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorResponse)) return false;
        if (!super.equals(o)) return false;

        ErrorResponse response = (ErrorResponse) o;

        return !(reason != null ? !reason.equals(response.reason) : response.reason != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        return result;
    }
}
