package channels;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;

public class Base64Channel implements Channel<byte[]> {
    private class Base64EncodedPacket extends PacketDecorator<byte[]> {
        public Base64EncodedPacket(Packet packet) {
            super(packet);
        }

        @Override
        public byte[] unpack() {
            final String data = DatatypeConverter.printBase64Binary(super.unpack());

            byte[] byteData;
            try {
                byteData = (data != null ? data.getBytes("UTF-8") : null);
            } catch (UnsupportedEncodingException e) {
                byteData = null;
            }

            return byteData;
        }
    }

    private class Base64DecodedPacket extends PacketDecorator<byte[]> {
        public Base64DecodedPacket(Packet<byte[]> packet) {
            super(packet);
        }

        @Override
        public byte[] unpack() {
            try {
                final String data = new String(super.unpack(), "UTF-8");
                return DatatypeConverter.parseBase64Binary(data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
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
