package channels;

import org.bouncycastle.util.encoders.Base64;

import java.util.logging.Logger;

/**
 * Can be used to decorate byte[] channels.
 * Encodes/decodes the transferred data as Base 64
 */
public class Base64Channel extends ChannelDecorator<byte[]> {
    public Base64Channel(Channel<byte[]> channel) {
        super(channel);
    }

    @Override
    public void send(Packet<byte[]> packet) throws ChannelException {
        final byte[] data = packet.unpack();
        packet.pack(Base64.encode(data));
        super.send(packet);
    }

    @Override
    public Packet<byte[]> receive() throws ChannelException {
        final Packet<byte[]> packet = super.receive();
        final byte[] data = packet.unpack();
        packet.pack(Base64.decode(data));
        return packet;
    }
    
    @Override
    public Channel getChannel() {
    	return this;
    }
}
