package executors;

import channels.Packet;
import messages.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageSender extends RepeatingTask {
    private final BlockingQueue<Packet<Message>> messages = new LinkedBlockingQueue<>();

    public void sendMessage(Packet<Message> message) {
        messages.add(message);
    }

    @Override
    protected void perform() throws InterruptedException {
        consumeMessage(messages.take());
    }

    protected abstract void consumeMessage(Packet<Message> message);
}