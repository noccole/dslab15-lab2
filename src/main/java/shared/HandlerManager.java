package shared;

import java.util.HashSet;
import java.util.Set;

public class HandlerManager {
    private final Set<HandlerBase> handlers = new HashSet<>();

    public void registerHandler(HandlerBase handler) {
        synchronized (handlers) {
            handlers.add(handler);
        }
    }

    public void unregisterHandler(HandlerBase handler) {
        synchronized (handlers) {
            handlers.remove(handler);
        }
    }

    public void stopAllHandlers() {
        final Set<HandlerBase> registeredHandlers;
        synchronized (handlers) {
            registeredHandlers = new HashSet<>(handlers);
        }

        for (HandlerBase handler : registeredHandlers) {
            handler.stop();
        }

        synchronized (handlers) {
            assert handlers.isEmpty();
        }
    }
}
