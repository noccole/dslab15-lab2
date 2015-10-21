package commands;

import java.io.Serializable;

public abstract class Response implements Serializable {
    private long requestId;

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }
}
