package commands;

import states.State;
import states.StateException;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Request implements Serializable {
    private static final AtomicLong requestCounter = new AtomicLong(0);

    private final long requestId;

    public Request() {
        requestId = requestCounter.incrementAndGet();
    }

    public long getRequestId() {
        return requestId;
    }

    public abstract State.StateResult applyTo(State state) throws StateException;
}
