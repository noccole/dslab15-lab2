package messages;

import java.util.concurrent.atomic.AtomicLong;

public abstract class Request implements Message {
    private static final AtomicLong requestCounter = new AtomicLong(0);

    private final long messageId;

    public Request() {
        messageId = requestCounter.incrementAndGet();
    }

    @Override
    public long getMessageId() {
        return messageId;
    }
}
