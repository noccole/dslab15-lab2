package executors;

import channels.NetworkPacket;
import channels.Packet;
import messages.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class MessageSender extends RepeatingTask {
    private final Lock messagesLock = new ReentrantLock();
    private final Condition messageQueueIsEmpty = messagesLock.newCondition();
    private final BlockingQueue<Packet<Message>> messages = new LinkedBlockingQueue<>();

    /**
     * Send the given message
     *
     * @param message Message which should be send
     */
    public void sendMessage(Packet<Message> message) {
        messagesLock.lock();
        messages.add(message);
        messagesLock.unlock();
    }

    @Override
    protected void perform() throws InterruptedException {
        final Packet<Message> message = messages.take();
        if (isCancelled()) {
            // a fake message was added in onCancelled
            messageQueueIsEmpty.signalAll();
            throw new InterruptedException();
        }

        consumeMessage(message);

        messagesLock.lock();
        if (messages.isEmpty()) {
            messageQueueIsEmpty.signalAll();
        }
        messagesLock.unlock();
    }

    /**
     * Will be called for each queued message which should be send.
     *
     * @param message
     */
    protected abstract void consumeMessage(Packet<Message> message);

    /**
     * Wait until all queued messages are send (blocks!)
     */
    public void waitForAllMessagesSend() {
        messagesLock.lock();
        if (!messages.isEmpty() && !isCancelled()) {
            messageQueueIsEmpty.awaitUninterruptibly();
        }
        messagesLock.unlock();
    }

    @Override
    protected void onCancelled() {
        sendMessage(new NetworkPacket<Message>()); // fake a packet to unblock perform

        super.onCancelled();
    }
}