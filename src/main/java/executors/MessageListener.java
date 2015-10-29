package executors;

import channels.Packet;
import messages.Message;

import java.util.HashSet;
import java.util.Set;

public abstract class MessageListener extends RepeatingTask {
    public interface EventHandler {
        void onMessageReceived(Packet<Message> message);
    }

    private final Set<EventHandler> eventHandlers = new HashSet<>();

    @Override
    protected void perform() throws InterruptedException {
        final Packet<Message> message = waitForMessage();
        if (message != null) {
            emitMessageReceived(message);
        } else {
            throw new InterruptedException();
        }
    }

    private void emitMessageReceived(Packet<Message> message) {
        synchronized (eventHandlers) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onMessageReceived(message);
            }
        }
    }

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

    /**
     *
     * @return (null package will throw a InterruptedExceptoin)
     */
    protected abstract Packet<Message> waitForMessage();
}
