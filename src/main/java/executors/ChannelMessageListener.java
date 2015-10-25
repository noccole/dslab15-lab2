package executors;

import channels.Channel;
import channels.ChannelException;
import channels.Packet;
import commands.Message;

public class ChannelMessageListener extends MessageListener {
    private final Channel<Message> channel;

    public ChannelMessageListener(Channel<Message> channel) {
        this.channel = channel;

        final MessageListener parent = this;
        channel.addEventHandler(new Channel.EventHandler() {
            @Override
            public void onChannelClosed() {
                parent.stop();
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

    @Override
    protected void onStopped() {
        try {
            channel.close();
        } catch (ChannelException e) {
            System.err.println("could not close channel: " + e);
        }

        super.onStopped();
    }
}
