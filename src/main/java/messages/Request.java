package messages;

import java.util.concurrent.atomic.AtomicLong;

public abstract class Request implements Message {
    private static final AtomicLong requestCounter = new AtomicLong(0);

    private final long messageId;

    public Request() {
        messageId = requestCounter.incrementAndGet();
    }

    public Request(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;

        Request request = (Request) o;

        return messageId == request.messageId;

    }

    @Override
    public int hashCode() {
        return (int) (messageId ^ (messageId >>> 32));
    }
}
