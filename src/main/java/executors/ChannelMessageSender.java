package executors;

import channels.Channel;
import channels.ChannelException;
import channels.Packet;
import commands.Message;

public class ChannelMessageSender extends MessageSender {
    private final Channel<Message> channel;

    public ChannelMessageSender(Channel<Message> channel) {
        this.channel = channel;

        final MessageSender parent = this;
        channel.addEventHandler(new Channel.EventHandler() {
            @Override
            public void onChannelClosed() {
                parent.stop();
            }
        });
    }

    @Override
    protected void consumeMessage(Packet<Message> message) {
        try {
            channel.send(message);
        } catch (ChannelException e) {
            System.err.println("could not send message " + e);
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
