package executors;

import channels.Packet;
import commands.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class MessageListener extends RepeatingTask {
    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    public interface EventHandler {
        void onMessageReceived(Packet<Message> message);
    }

    private final Set<EventHandler> eventHandlers = new HashSet<>();

    public MessageListener() {
        executorService.submit(this);
    }

    @Override
    protected void perform() {
        final Packet<Message> message = waitForMessage();
        if (message != null) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onMessageReceived(message);
            }
        } else {
            stop();
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
