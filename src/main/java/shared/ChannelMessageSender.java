package shared;

import channels.Channel;
import channels.ChannelException;
import channels.Packet;
import messages.Message;

import java.util.logging.Logger;

public class ChannelMessageSender extends MessageSender {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final Channel<Message> channel;

    public ChannelMessageSender(Channel<Message> channel) {
        this.channel = channel;

        final MessageSender parent = this;
        channel.addEventHandler(new Channel.EventHandler() {
            @Override
            public void onChannelClosed() {
                LOGGER.info("ChannelMessageSender -> Channel::onChannelClosed");
                parent.cancel(true);
            }
        });
    }

    @Override
    protected void consumeMessage(Packet<Message> message) {
        LOGGER.info("ChannelMessageSender::consumeMessage with parameters: " + message);

        try {
            channel.send(message);
        } catch (ChannelException e) {
            LOGGER.warning("could not send message " + e);
        }
    }
}
