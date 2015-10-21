package channels;

public class ChannelDecorator<T> implements Channel<T> {
    private final Channel channel;

    public ChannelDecorator(Channel<T> channel) {
        this.channel = channel;
    }

    @Override
    public void send(Packet<T> packet) throws ChannelException {
        channel.send(packet);
    }

    @Override
    public Packet<T> receive() throws ChannelException {
        return channel.receive();
    }
}
