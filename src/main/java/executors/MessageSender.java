package executors;

import channels.Packet;
import commands.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageSender extends RepeatingTask {
    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    private final BlockingQueue<Packet<Message>> messages = new LinkedBlockingQueue<>();

    public MessageSender() {
        executorService.submit(this);
    }

    public void sendMessage(Packet<Message> message) {
        messages.add(message);
    }

    @Override
    protected void perform() {
        try {
            consumeMessage(messages.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected abstract void consumeMessage(Packet<Message> message);
}