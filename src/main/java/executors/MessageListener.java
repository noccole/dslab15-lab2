package executors;

import channels.Packet;
import commands.Message;

import java.util.HashSet;
import java.util.Set;

public abstract class MessageListener extends RepeatingTask {
    public interface EventHandler {
        void onMessageReceived(Packet<Message> message);
    }

    private final Set<EventHandler> eventHandlers = new HashSet<>();

    @Override
    protected void perform() {
        final Packet<Message> message = waitForMessage();
        if (message != null) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onMessageReceived(message);
            }
        }
    }

    public void addEventHandler(EventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public void removeEventHandler(EventHandler eventHandler) {
        eventHandlers.remove(eventHandler);
    }

    protected abstract Packet<Message> waitForMessage();
}
