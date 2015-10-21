package executors;

import channels.ChannelException;
import channels.Channel;
import channels.Packet;
import commands.Request;

public class ChannelRequestListener extends RequestListener {
    private final Channel<Request> channel;

    public ChannelRequestListener(Channel<Request> channel) {
        this.channel = channel;
    }

    @Override
    protected Packet<Request> waitForRequest() {
        try {
            return channel.receive();
        } catch (ChannelException e) {
            return null;
        }
    }
}
