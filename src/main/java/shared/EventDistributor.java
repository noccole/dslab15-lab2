package shared;


import channels.NetworkPacket;
import channels.Packet;
import messages.Event;
import messages.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class EventDistributor {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final Set<MessageSender> senders = new HashSet<>();

    /**
     * Publish the given \a event
     * Hands over the event to all registered senders.
     *
     * @param event Event which should be published
     */
    public void publish(Event event) {
        final Packet<Message> packet = new NetworkPacket<>();
        packet.pack(event);

        synchronized (senders) {
            for (MessageSender sender : senders) {
                try {
                    sender.sendMessage(packet);
                } catch (TaskCancelledException e) {
                    LOGGER.warning("Sender '" + sender + "' was cancelled");
                }
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
        final Packet<Message> packet = new NetworkPacket<>();
        packet.pack(event);

        synchronized (senders) {
            for (MessageSender sender : senders) {
                if (ignoredSenders.contains(sender)) {
                    continue;
                }

                try {
                    sender.sendMessage(packet);
                } catch (TaskCancelledException e) {
                    LOGGER.warning("Sender '" + sender + "' was cancelled");
                }
            }
        }
    }

    /**
     * Register the message sender \a sender to receive published events.
     *
     * @param sender Message sender which should receive published events
     */
    public void subscribe(final MessageSender sender) {
        synchronized (senders) {
            senders.add(sender);
        }

        sender.addEventHandler(new RepeatingTask.EventHandler() {
            @Override
            public void onCancelled() {
                unsubscribe(sender);
            }
        });
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
}
