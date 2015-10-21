package executors;

import channels.NetworkPacket;
import channels.Packet;
import commands.Request;
import commands.Response;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class RequestHandler implements Runnable {
    public interface EventHandler {
        void onRequestHandled(Packet<Response> response);
    }

    private Set<EventHandler> eventHandlers = new HashSet<>();
    private final BlockingQueue<Packet<Request>> requests = new LinkedBlockingQueue<>();
    private boolean run = true;

    public void handleRequest(Packet<Request> request) {
        requests.add(request);
    }

    @Override
    public void run() {
        while (run) {
            try {
                Packet<Request> requestPacket = requests.take();

                Request request = requestPacket.unpack();
                Response response = consumeRequest(request);

                Packet<Response> responsePacket = new NetworkPacket<>();
                responsePacket.setRemoteAddress(requestPacket.getRemoteAddress());
                responsePacket.pack(response);

                for (EventHandler eventHandler : eventHandlers) {
                    eventHandler.onRequestHandled(responsePacket);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addEventHandler(EventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public void removeEventHandler(EventHandler eventHandler) {
        eventHandlers.remove(eventHandler);
    }

    protected abstract Response consumeRequest(Request request);

    public void stop() {
        run = false;
    }
}
