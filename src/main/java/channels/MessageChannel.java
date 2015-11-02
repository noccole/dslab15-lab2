package channels;

import messages.Message;
import messages.UnknownRequest;

import java.io.*;

/**
 * Sends/receives Messages via/from a byte[] channel
 */
public class MessageChannel implements Channel<Message> {
    private final Channel<byte[]> channel;

    public MessageChannel(Channel<byte[]> channel) {
        this.channel = channel;
    }

    @Override
    public void send(Packet<Message> packet) throws ChannelException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            final Message message = packet.unpack();
            ObjectOutputStream objectStream =  new ObjectOutputStream(byteStream);
            objectStream.writeObject(message);
        } catch (IOException e) {
            throw new ChannelException("could not serialize message: " + packet.unpack(), e);
        }

        final Packet<byte[]> bytePacket = new NetworkPacket();
        bytePacket.setRemoteAddress(packet.getRemoteAddress());
        bytePacket.pack(byteStream.toByteArray());
        channel.send(bytePacket);
    }

    @Override
    public Packet<Message> receive() throws ChannelException {
        final Packet<byte[]> bytePacket = channel.receive();

        Message message;
        try {
            final InputStream byteStream = new ByteArrayInputStream(bytePacket.unpack());
            final ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            message = Message.class.cast(objectStream.readObject());
        } catch (IOException e) {
            final UnknownRequest unknownRequest = new UnknownRequest();
            unknownRequest.setRequestData(bytePacket.unpack());
            unknownRequest.setReason("could not deserialize message");
            message = unknownRequest;
        } catch (ClassNotFoundException e) {
            final UnknownRequest unknownRequest = new UnknownRequest();
            unknownRequest.setRequestData(bytePacket.unpack());
            unknownRequest.setReason("received object was not of type Message");
            message = unknownRequest;
        }

        final Packet<Message> packet = new NetworkPacket();
        packet.setRemoteAddress(bytePacket.getRemoteAddress());
        packet.pack(message);
        return packet;
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
}
