package channels;

import java.util.Base64;

public class Base64Channel implements Channel<byte[]> {
    private class Base64EncodedPacket extends PacketDecorator<byte[]> {
        public Base64EncodedPacket(Packet packet) {
            super(packet);
        }

        @Override
        public byte[] unpack() {
            return Base64.getEncoder().encode(super.unpack());
        }
    }

    private class Base64DecodedPacket extends PacketDecorator<byte[]> {
        public Base64DecodedPacket(Packet packet) {
            super(packet);
        }

        @Override
        public byte[] unpack() {
            return Base64.getDecoder().decode(super.unpack());
        }
    }

    private final Channel channel;

    public Base64Channel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void send(Packet packet) throws ChannelException {
        channel.send(new Base64EncodedPacket(packet));
    }

    @Override
    public Packet receive() throws ChannelException {
        return new Base64DecodedPacket(channel.receive());
    }
}
