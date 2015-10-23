package channels;

import javax.xml.bind.DatatypeConverter;

public class Base64Channel implements Channel<byte[]> {
    private class Base64EncodedPacket extends PacketDecorator<byte[]> {
        public Base64EncodedPacket(Packet packet) {
            super(packet);
        }

        @Override
        public byte[] unpack() {
            final String data = DatatypeConverter.printBase64Binary(super.unpack());

            try {
                return Encoder.decodeString(data);
            } catch (ChannelException e) {
                System.err.println("could not encode package: " + e);
                return super.unpack();
            }
        }
    }

    private class Base64DecodedPacket extends PacketDecorator<byte[]> {
        public Base64DecodedPacket(Packet<byte[]> packet) {
            super(packet);
        }

        @Override
        public byte[] unpack() {
            try {
                final String data = Encoder.encodeByteArray(super.unpack());
                return DatatypeConverter.parseBase64Binary(data);
            } catch (ChannelException e) {
                System.err.println("could not decode package: " + e);
                return super.unpack();
            }
        }
    }

    private final Channel<byte[]> channel;

    public Base64Channel(Channel<byte[]> channel) {
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
