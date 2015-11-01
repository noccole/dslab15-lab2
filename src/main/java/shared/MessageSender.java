package shared;

import channels.Packet;
import messages.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageSender extends RepeatingTask {
    private final BlockingQueue<Packet<Message>> messages = new LinkedBlockingQueue<>();

    /**
     * Send the given message
     *
     * @param message Message which should be send
     */
    public void sendMessage(Packet<Message> message) throws TaskCancelledException {
        if (isCancelled()) {
            throw new TaskCancelledException("MessageSender was cancelled");
        }

        messages.add(message);
    }

    @Override
    protected void perform() throws InterruptedException {
        consumeMessage(messages.take());
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

    /**
     * Will be called for each queued message which should be send.
     *
     * @param message
     */
    protected abstract void consumeMessage(Packet<Message> message);
}