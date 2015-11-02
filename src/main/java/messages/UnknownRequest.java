package messages;

import states.State;
import states.StateException;
import states.StateResult;

public class UnknownRequest implements Message {
    private byte[] requestData;
    private String reason;

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
    public long getMessageId() {
        return -1;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleUnknownRequest(this);
    }

    @Override
    public String toString() {
        return "unknown request";
    }
}
