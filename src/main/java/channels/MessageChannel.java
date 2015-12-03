package channels;

import messages.Message;
import messages.TamperedRequest;
import messages.UnknownRequest;

import java.io.*;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

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
        final Packet<byte[]> bytePacket;
        try {
            bytePacket = channel.receive();
        } catch (ChannelIntegrityException e) {
            final Message message = toMessage(e.getBytes());

            final Packet<Message> packet = new NetworkPacket();
            packet.pack(new TamperedRequest(message));
            return packet;
        }

        final Message message = toMessage(bytePacket.unpack());

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
    
    public Channel getChannel() {
    	return channel.getChannel();
    }

    private Message toMessage(byte[] data) {
        try {
            final InputStream byteStream = new ByteArrayInputStream(data);
            final ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            return Message.class.cast(objectStream.readObject());
        } catch (IOException e) {
            final UnknownRequest unknownRequest = new UnknownRequest();
            unknownRequest.setRequestData(data);
            unknownRequest.setReason("could not deserialize message");
            return unknownRequest;
        } catch (ClassNotFoundException e) {
            final UnknownRequest unknownRequest = new UnknownRequest();
            unknownRequest.setRequestData(data);
            unknownRequest.setReason("received object was not of type Message");
            return unknownRequest;
        }
    }
}
