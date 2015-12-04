package channels;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import messages.Message;
import messages.TamperedRequest;
import messages.UnknownRequest;

import java.io.*;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Logger;

/**
 * Sends/receives Messages via/from a byte[] channel
 */
public class MessageChannel implements Channel<Message> {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final Channel<byte[]> channel;
    private final MessageMarshaller messageMarshaller;

    public MessageChannel(MessageMarshaller messageMarshaller, Channel<byte[]> channel) {
        this.messageMarshaller = messageMarshaller;
        this.channel = channel;
    }

    @Override
    public void send(Packet<Message> packet) throws ChannelException {
        final byte[] data;
        try {
            data = messageMarshaller.marshall(packet.unpack());
        } catch (MarshallingException e) {
            throw new ChannelException("Could not marshall message: " + packet.unpack(), e);
        }

        final Packet<byte[]> bytePacket = new NetworkPacket();
        bytePacket.setRemoteAddress(packet.getRemoteAddress());
        bytePacket.pack(data);
        channel.send(bytePacket);
    }

    @Override
    public Packet<Message> receive() throws ChannelException {
        final Packet<byte[]> bytePacket;
        try {
            bytePacket = channel.receive();
        } catch (ChannelIntegrityException e) {
            Message message;
            try {
                message = messageMarshaller.unmarshall(e.getBytes());
            } catch (MarshallingException e1) {
                LOGGER.warning("Could not unmarshall data: " + e.getBytes());
                message = null;
            }

            final Packet<Message> packet = new NetworkPacket();
            packet.pack(new TamperedRequest(message));
            return packet;
        }

        final Message message;
        try {
            message = messageMarshaller.unmarshall(bytePacket.unpack());
        } catch (MarshallingException e) {
            throw new ChannelException("Could not unmarshall data: " + bytePacket.unpack(), e);
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
