package shared;

import channels.NetworkPacket;
import channels.Packet;
import messages.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageHandler extends RepeatingTask {
    public interface EventHandler extends RepeatingTask.EventHandler {
        /**
         * Will be emitted whenever a message has be handled and a result was produced.
         *
         * @param message Message which was handled.
         * @param result Result which was computed by handling the Message \a message.
         */
        void onMessageHandled(Packet<Message> message, Packet<Message> result);
    }

    private final Set<EventHandler> eventHandlers = new HashSet<>();
    private final BlockingQueue<Packet<Message>> messages = new LinkedBlockingQueue<>();

    /**
     * Handles the given message
     *
     * @param message Message which should be handled
     */
    public void handleMessage(Packet<Message> message) throws TaskCancelledException {
        if (isCancelled()) {
            throw new TaskCancelledException("MessageHandler was cancelled");
        }

        messages.add(message);
    }

    @Override
    protected void perform() throws InterruptedException {
        final Packet<Message> requestPacket = messages.take();

        final Message request = requestPacket.unpack();
        final Message response = consumeMessage(request);

        if (response != null) {
            final Packet<Message> responsePacket = new NetworkPacket<>();
            responsePacket.setRemoteAddress(requestPacket.getRemoteAddress());
            responsePacket.pack(response);

            emitMessageHandled(requestPacket, responsePacket);
        }
    }

    @Override
    protected void onExit() {
        while (!messages.isEmpty()) {
            try {
                perform();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void emitMessageHandled(Packet<Message> message, Packet<Message> result) {
        synchronized (eventHandlers) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onMessageHandled(message, result);
            }
        }
    }

    public void addEventHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.add(eventHandler);
        }

        super.addEventHandler(eventHandler);
    }

    public void removeEventHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.remove(eventHandler);
        }

        super.removeEventHandler(eventHandler);
    }

    /**
     * Will be called for each queued message which should be handled.
     *
     * @param message Message which should be handled
     * @return Result message or null if no result
     */
    protected abstract Message consumeMessage(Message message);
}
