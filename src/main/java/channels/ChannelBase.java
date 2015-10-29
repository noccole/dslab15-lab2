package channels;

import java.util.HashSet;
import java.util.Set;

public abstract class ChannelBase<T> implements Channel<T> {
    private final Set<EventHandler> eventHandlers = new HashSet<>();

    @Override
    public void addEventHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.add(eventHandler);
        }
    }

    @Override
    public void removeEventHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.remove(eventHandler);
        }
    }

    /**
     * Emit the onChannelClosed signal
     */
    protected void emitChannelClosed() {
        synchronized (eventHandlers) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onChannelClosed();
            }
        }
    }
}
