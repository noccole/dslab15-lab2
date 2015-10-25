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
    protected void perform() throws InterruptedException {
        final Packet<Message> message = waitForMessage();
        if (message != null) {
            synchronized (eventHandlers) {
                for (EventHandler eventHandler : eventHandlers) {
                    eventHandler.onMessageReceived(message);
                }
            }
        } else {
            throw new InterruptedException();
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
