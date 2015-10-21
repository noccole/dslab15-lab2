package executors;

import channels.Packet;
import commands.Request;

import java.util.HashSet;
import java.util.Set;

public abstract class RequestListener implements Runnable {
    public interface EventHandler {
        void onRequestReceived(Packet<Request> request);
    }

    private Set<EventHandler> eventHandlers = new HashSet<>();
    private boolean run = true;

    @Override
    public void run() {
        while (run) {
            final Packet<Request> request = waitForRequest();
            if (request != null) {
                for (EventHandler eventHandler : eventHandlers) {
                    eventHandler.onRequestReceived(request);
                }
            } else {
                stop();
            }
        }
    }

    public void addEventHandler(EventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public void removeEventHandler(EventHandler eventHandler) {
        eventHandlers.remove(eventHandler);
    }

    protected abstract Packet<Request> waitForRequest();

    public void stop() {
        run = false;
    }
}
