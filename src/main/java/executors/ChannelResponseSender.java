package executors;

import channels.ChannelException;
import channels.Channel;
import channels.Packet;
import commands.Request;
import commands.Response;

public class ChannelResponseSender extends ResponseSender {
    private final Channel<Response> channel;

    public ChannelResponseSender(Channel<Response> channel) {
        this.channel = channel;
    }

    @Override
    protected void consumeResponse(Packet<Response> response) {
        try {
            channel.send(response);
        } catch (ChannelException e) {
            System.err.println("could not send command " + e);
        }
    }
}
