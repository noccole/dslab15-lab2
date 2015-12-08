package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

import java.util.Arrays;

public class UnknownRequest extends Request {
    private byte[] requestData;
    private String reason;

    public UnknownRequest() {
        super(-1);
    }

    public byte[] getRequestData() {
        return requestData;
    }

    public void setRequestData(byte[] requestData) {
        this.requestData = requestData;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleUnknownRequest(this);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public String toString() {
        return "unknown request";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnknownRequest)) return false;
        if (!super.equals(o)) return false;

        UnknownRequest that = (UnknownRequest) o;

        if (!Arrays.equals(requestData, that.requestData)) return false;
        return !(reason != null ? !reason.equals(that.reason) : that.reason != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(requestData);
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        return result;
    }
}
