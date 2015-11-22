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

    @Override
    public void close() throws ChannelException {
        channel.close();
    }

    @Override
    public void addEventHandler(EventHandler eventHandler) {
        channel.addEventHandler(eventHandler);
    }

    @Override
    public void removeEventHandler(EventHandler eventHandler) {
        channel.removeEventHandler(eventHandler);
    }
    
    @Override
    public Channel getChannel() {
    	return channel;
    }
}
