package executors;

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

    public void sendMessage(Packet<Message> message) {
        messagesLock.lock();
        messages.add(message);
        messagesLock.unlock();
    }

    @Override
    protected void perform() throws InterruptedException {
        consumeMessage(messages.take());

        messagesLock.lock();
        if (messages.isEmpty()) {
            messageQueueIsEmpty.signal();
        }
        messagesLock.unlock();
    }

    protected abstract void consumeMessage(Packet<Message> message);

    public void waitForAllMessagesSend() {
        messagesLock.lock();
        if (!messages.isEmpty()) {
            messageQueueIsEmpty.awaitUninterruptibly();
        }
        messagesLock.unlock();
    }
}