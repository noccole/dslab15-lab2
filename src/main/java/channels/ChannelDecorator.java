package channels;

public class ChannelDecorator<T> implements Channel {
    private final Channel channel;

    public ChannelDecorator(Channel<T> channel) {
        this.channel = channel;
    }

    @Override
    public void send(Packet packet) throws ChannelException {
        channel.send(packet);
    }

    @Override
    public Packet receive() throws ChannelException {
        return channel.receive();
    }
}
