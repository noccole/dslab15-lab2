package executors;

import channels.Packet;
import commands.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageSender implements Runnable {
    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    private final BlockingQueue<Packet<Message>> messages = new LinkedBlockingQueue<>();
    private boolean run = true;

    public MessageSender() {
        executorService.submit(this);
    }

    public void sendMessage(Packet<Message> message) {
        messages.add(message);
    }

    @Override
    public void run() {
        while (run) {
            try {
                consumeMessage(messages.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void consumeMessage(Packet<Message> message);

    public void stop() {
        run = false;
    }
}