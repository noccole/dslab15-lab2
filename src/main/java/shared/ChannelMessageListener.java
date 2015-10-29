package shared;

import channels.Channel;
import channels.ChannelException;
import channels.Packet;
import messages.Message;

import java.util.logging.Logger;

public class ChannelMessageListener extends MessageListener {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final Channel<Message> channel;

    public ChannelMessageListener(Channel<Message> channel) {
        this.channel = channel;

        final MessageListener parent = this;
        channel.addEventHandler(new Channel.EventHandler() {
            @Override
            public void onChannelClosed() {
                LOGGER.info("ChannelMessageListener -> Channel::onChannelClosed");
                parent.cancel(true);
            }
        });
    }

    @Override
    protected Packet<Message> waitForMessage() {
        try {
            final Packet<Message> packet = channel.receive();
            LOGGER.info("ChannelMessageListener::waitForMessage received packet: " + packet);
            return packet;
        } catch (ChannelException e) {
            return null;
        }
    }
}
