package client;

import shared.HandlerBase;

import java.util.HashSet;
import java.util.Set;

abstract class ClientHandlerBase extends HandlerBase {
    public interface EventHandler {
        void onMessageReceived(String message);
        void onExit();
    }

    private final Set<EventHandler> eventHandlers = new HashSet<>();

    public void addEventHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.add(eventHandler);
        }
    }

    public void removeEventHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.remove(eventHandler);
        }
    }

    protected void emitMessageReceived(String message) {
        synchronized (eventHandlers) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onMessageReceived(message);
            }
        }
    }

    protected void emitExit() {
        synchronized (eventHandlers) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onExit();
            }
        }
    }
}