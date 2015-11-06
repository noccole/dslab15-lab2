package channels;

import org.bouncycastle.util.encoders.Base64;

import java.util.logging.Logger;

/**
 * Can be used to decorate byte[] channels.
 * Encodes/decodes the transferred data as Base 64
 */
public class Base64Channel extends ChannelDecorator<byte[]> {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private class Base64EncodedPacket extends PacketDecorator<byte[]> {
        public Base64EncodedPacket(Packet packet) {
            super(packet);
        }

        @Override
        public byte[] unpack() {
            return Base64.encode(super.unpack());
        }
    }

    private class Base64DecodedPacket extends PacketDecorator<byte[]> {
        public Base64DecodedPacket(Packet<byte[]> packet) {
            super(packet);
        }

        @Override
        public byte[] unpack() {
            return Base64.decode(super.unpack());
        }
    }

    public Base64Channel(Channel<byte[]> channel) {
        super(channel);
    }

    @Override
    public void send(Packet packet) throws ChannelException {
        super.send(new Base64EncodedPacket(packet));
    }

    @Override
    public Packet receive() throws ChannelException {
        return new Base64DecodedPacket(super.receive());
    }
}
