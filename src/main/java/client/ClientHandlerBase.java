package client;

import shared.HandlerBase;

import java.util.HashSet;
import java.util.Set;

abstract class ClientHandlerBase extends HandlerBase {
    public interface EventHandler {
        void onMessageReceived(String message);
        void onTamperedMessageReceived(String message);
        void onPresenceChanged(String presenceMessage);
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

    protected void emitTamperedMessageReceived(String message) {
        synchronized (eventHandlers) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onTamperedMessageReceived(message);
            }
        }
    }

    protected void emitPresenceChanged(String presenceMessage) {
        synchronized (eventHandlers) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onPresenceChanged(presenceMessage);
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
