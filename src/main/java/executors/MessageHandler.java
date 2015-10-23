package executors;

import channels.NetworkPacket;
import channels.Packet;
import commands.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageHandler implements Runnable {
    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    public interface EventHandler {
        void onMessageHandled(Packet<Message> message);
        void onMessageHandled(Packet<Message> message, Packet<Message> result);
    }

    private final Set<EventHandler> eventHandlers = new HashSet<>();
    private final BlockingQueue<Packet<Message>> messages = new LinkedBlockingQueue<>();
    private boolean run = true;

    public MessageHandler() {
        executorService.submit(this);
    }

    public void handleMessage(Packet<Message> message) {
        messages.add(message);
    }

    @Override
    public void run() {
        while (run) {
            try {
                final Packet<Message> requestPacket = messages.take();

                final Message request = requestPacket.unpack();
                final Message response = consumeMessage(request);

                if (response != null) {
                    final Packet<Message> responsePacket = new NetworkPacket<>();
                    responsePacket.setRemoteAddress(requestPacket.getRemoteAddress());
                    responsePacket.pack(response);

                    for (EventHandler eventHandler : eventHandlers) {
                        eventHandler.onMessageHandled(requestPacket, responsePacket);
                    }
                } else {
                    for (EventHandler eventHandler : eventHandlers) {
                        eventHandler.onMessageHandled(requestPacket);
                    }
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

    protected abstract Message consumeMessage(Message message);

    public void stop() {
        run = false;
    }
}
