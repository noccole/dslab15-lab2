package channels;

public interface Channel<T> {
    void send(Packet<T> packet) throws ChannelException;

    Packet<T> receive() throws ChannelException;
}
