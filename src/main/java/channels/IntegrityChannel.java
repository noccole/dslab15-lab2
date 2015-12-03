package channels;

import org.bouncycastle.util.encoders.Base64;
import util.ArrayUtils;

import javax.crypto.Mac;
import java.security.MessageDigest;
import java.util.List;

public class IntegrityChannel extends ChannelDecorator<byte[]> {
    private static final byte DELIMITER = (byte)32; // space

    private final Mac hashMac;

    public IntegrityChannel(Mac hashMac, Channel<byte[]> channel) {
        super(channel);

        this.hashMac = hashMac;
    }

    @Override
    public void send(Packet<byte[]> packet) throws ChannelException {
        final byte[] data = packet.unpack();
        final byte[] hash = Base64.encode(hashMac.doFinal(data));

        final byte[] delimiterByteArray = new byte[1];
        delimiterByteArray[0] = DELIMITER;

        packet.pack(ArrayUtils.join(hash, delimiterByteArray, data));
        super.send(packet);
    }

    @Override
    public Packet<byte[]> receive() throws ChannelException {
        final Packet<byte[]> packet = super.receive();

        final List<byte[]> parts = ArrayUtils.split(packet.unpack(), DELIMITER, 2);
        if (parts.size() != 2) {
            throw new ChannelIntegrityException("Wrong number of message parts, must be '<hash> <data>'!", packet.unpack());
        }

        final byte[] receivedHash = Base64.decode(parts.get(0));
        final byte[] data = parts.get(1);

        final byte[] computedHash = hashMac.doFinal(data);
        if (!MessageDigest.isEqual(computedHash, receivedHash)) {
            throw new ChannelIntegrityException("Received hash and computed hash do not match!", data);
        }

        packet.pack(data);
        return packet;
    }
}
