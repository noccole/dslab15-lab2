package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

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
}
