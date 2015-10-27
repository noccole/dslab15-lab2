package executors;

import channels.NetworkPacket;
import channels.Packet;
import messages.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageHandler extends RepeatingTask {
    public interface EventHandler {
        void onMessageHandled(Packet<Message> message, Packet<Message> result);
    }

    private final Set<EventHandler> eventHandlers = new HashSet<>();
    private final BlockingQueue<Packet<Message>> messages = new LinkedBlockingQueue<>();

    public void handleMessage(Packet<Message> message) {
        messages.add(message);
    }

    @Override
    protected void perform() throws InterruptedException {
        final Packet<Message> requestPacket = messages.take();
        if (isCancelled()) {
            // a fake message was added in onCancelled
            throw new InterruptedException();
        }

        final Message request = requestPacket.unpack();
        final Message response = consumeMessage(request);

        if (response != null) {
            final Packet<Message> responsePacket = new NetworkPacket<>();
            responsePacket.setRemoteAddress(requestPacket.getRemoteAddress());
            responsePacket.pack(response);

            synchronized (eventHandlers) {
                for (EventHandler eventHandler : eventHandlers) {
                    eventHandler.onMessageHandled(requestPacket, responsePacket);
                }
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

    @Override
    protected void onCancelled() {
        handleMessage(new NetworkPacket<Message>()); // fake a packet to unblock perform

        super.onCancelled();
    }

    protected abstract Message consumeMessage(Message message);
}
