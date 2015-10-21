package channels;

import commands.*;

import java.io.*;

public class CommandChannel implements Channel<Request> {
    private final Channel<byte[]> channel;

    public CommandChannel(Channel<byte[]> channel) {
        this.channel = channel;
    }

    @Override
    public void send(Packet<Request> packet) throws ChannelException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            final Request request = packet.unpack();
            ObjectOutputStream objectStream =  new ObjectOutputStream(byteStream);
            objectStream.writeObject(request);
        } catch (IOException e) {
            throw new ChannelException("could not serialize command", e);
        }

        final Packet<byte[]> bytePacket = new NetworkPacket();
        bytePacket.setRemoteAddress(packet.getRemoteAddress());
        bytePacket.pack(byteStream.toByteArray());
        channel.send(bytePacket);
    }

    @Override
    public Packet receive() throws ChannelException {
        final Packet<byte[]> bytePacket = channel.receive();

        Request request;
        try {
            InputStream byteStream = new ByteArrayInputStream(bytePacket.unpack());
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            request = (Request)objectStream.readObject();
        } catch (IOException e) {
            throw new ChannelException("could not deserialize command", e);
        } catch (ClassNotFoundException e) {
            throw new ChannelException("received object was not of type ICommand", e);
        }

        final Packet<Request> packet = new NetworkPacket();
        packet.setRemoteAddress(bytePacket.getRemoteAddress());
        packet.pack(request);
        return packet;
    }
}
