package executors;

import channels.Channel;
import channels.ChannelException;
import channels.Packet;
import commands.Message;

public class ChannelMessageListener extends MessageListener {
    private final Channel<Message> channel;

    public ChannelMessageListener(Channel<Message> channel) {
        this.channel = channel;
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
