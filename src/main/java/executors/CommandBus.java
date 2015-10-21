package executors;

import channels.Packet;
import commands.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandBus {
    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    public CommandBus(MessageListener listener, final MessageHandler handler, final MessageSender sender) {
        handler.addEventHandler(new MessageHandler.EventHandler() {
            @Override
            public void onMessageHandled(Packet<Message> message) {
                // nothing to do
            }
            @Override
            public void onMessageHandled(Packet<Message> message, Packet<Message> result) {
                sender.sendMessage(result);
            }
        });

        listener.addEventHandler(new MessageListener.EventHandler() {
            @Override
            public void onMessageReceived(Packet<Message> message) {
                handler.handleMessage(message);
            }
        });

        executorService.submit(sender);
        executorService.submit(handler);
        executorService.submit(listener);
    }
}
