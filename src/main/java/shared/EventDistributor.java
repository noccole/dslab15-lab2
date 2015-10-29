package shared;


import channels.NetworkPacket;
import channels.Packet;
import messages.Event;
import messages.Message;

import java.util.HashSet;
import java.util.Set;

public class EventDistributor {
    private final Set<MessageSender> senders = new HashSet<>();

    /**
     * Publish the given \a event
     * Hands over the event to all registered senders.
     *
     * @param event Event which should be published
     */
    public void publish(Event event) {
        Packet<Message> packet = new NetworkPacket<>();
        packet.pack(event);

        synchronized (senders) {
            for (MessageSender sender : senders) {
                sender.sendMessage(packet);
            }
        }
    }

    /**
     * Publish the given \a event
     * Hands over the event to all registered senders.
     *
     * @param event Event which should be published
     * @param ignoredSenders Senders which should be ignored (= not receive this event)
     */
    public void publish(Event event, Set<MessageSender> ignoredSenders) {
        Packet<Message> packet = new NetworkPacket<>();
        packet.pack(event);

        synchronized (senders) {
            for (MessageSender sender : senders) {
                if (ignoredSenders.contains(sender)) {
                    continue;
                }

                sender.sendMessage(packet);
            }
        }
    }

    /**
     * Register the message sender \a sender to receive published events.
     *
     * @param sender Message sender which should receive published events
     */
    public void subscribe(MessageSender sender) {
        synchronized (senders) {
            senders.add(sender);
        }
    }

    /**
     * Unregister the already registered message sender \a sender to not receive published events anymore.
     *
     * @param sender Message sender which should not receive published events anymore
     */
    public void unsubscribe(MessageSender sender) {
        synchronized (senders) {
            senders.remove(sender);
        }
    }

    /**
     * Waits unit all published events are send (blocks!)
     */
    public void waitForAllMessagesSend() {
        synchronized (senders) {
            for (MessageSender sender : senders) {
                sender.waitForAllMessagesSend();
            }
        }
    }
}
