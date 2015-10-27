package executors;

import channels.Channel;
import channels.ChannelException;
import channels.Packet;
import messages.Message;

public class ChannelMessageListener extends MessageListener {
    private final Channel<Message> channel;

    public ChannelMessageListener(Channel<Message> channel) {
        this.channel = channel;

        final MessageListener parent = this;
        channel.addEventHandler(new Channel.EventHandler() {
            @Override
            public void onChannelClosed() {
                parent.cancel(true);
            }
        });
    }

    @Override
    protected Packet<Message> waitForMessage() {
        try {
            return channel.receive();
        } catch (ChannelException e) {
            return null;
        }
    }
}
