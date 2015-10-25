package executors;

import channels.Packet;
import commands.Message;

import java.util.HashSet;
import java.util.Set;

public class CommandBus {
    private final Set<MessageListener> listeners = new HashSet<>();
    private final Set<MessageHandler> handlers = new HashSet<>();
    private final Set<MessageSender> senders = new HashSet<>();

    public void addMessageListener(MessageListener listener) {
        for (final MessageHandler handler : handlers) {
            listener.addEventHandler(new MessageListener.EventHandler() {
                @Override
                public void onMessageReceived(Packet<Message> message) {
                    handler.handleMessage(message);
                }
            });
        }

        listeners.add(listener);
    }

    public void addMessageHandler(final MessageHandler handler) {
        for (MessageListener listener : listeners) {
            listener.addEventHandler(new MessageListener.EventHandler() {
                @Override
                public void onMessageReceived(Packet<Message> message) {
                    handler.handleMessage(message);
                }
            });
        }

        for (final MessageSender sender : senders) {
            handler.addEventHandler(new MessageHandler.EventHandler() {
                @Override
                public void onMessageHandled(Packet<Message> message, Packet<Message> result) {
                    sender.sendMessage(result);
                }
            });
        }

        handlers.add(handler);
    }

    public void addMessageSender(final MessageSender sender) {
        for (MessageHandler handler : handlers) {
            handler.addEventHandler(new MessageHandler.EventHandler() {
                @Override
                public void onMessageHandled(Packet<Message> message, Packet<Message> result) {
                    sender.sendMessage(result);
                }
            });
        }

        senders.add(sender);
    }
}
