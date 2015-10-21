package executors;

import channels.Packet;
import commands.Request;
import commands.Response;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class ResponseSender implements Runnable {
    private final BlockingQueue<Packet<Response>> responses = new LinkedBlockingQueue<>();
    private boolean run = true;

    public void sendResponse(Packet<Response> response) {
        responses.add(response);
    }

    @Override
    public void run() {
        while (run) {
            try {
                consumeResponse(responses.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void consumeResponse(Packet<Response> command);

    public void stop() {
        run = false;
    }
}