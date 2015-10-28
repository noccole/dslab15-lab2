package executors;


import channels.NetworkPacket;
import channels.Packet;
import messages.Event;
import messages.Message;

import java.util.HashSet;
import java.util.Set;

public class EventDistributor {
    private final Set<MessageSender> senders = new HashSet<>();

    public void publish(Event event) {
        Packet<Message> packet = new NetworkPacket<>();
        packet.pack(event);

        synchronized (senders) {
            for (MessageSender sender : senders) {
                sender.sendMessage(packet);
            }
        }
    }

    public void subscribe(MessageSender sender) {
        synchronized (senders) {
            senders.add(sender);
        }
    }

    public void unsubscribe(MessageSender sender) {
        synchronized (senders) {
            senders.remove(sender);
        }
    }

    public void waitForAllMessagesSend() {
        synchronized (senders) {
            for (MessageSender sender : senders) {
                sender.waitForAllMessagesSend();
            }
        }
    }
}
