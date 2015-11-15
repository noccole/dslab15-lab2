package channels;

import org.junit.Test;
import util.Keys;
import util.Utf8;

import javax.crypto.Mac;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IntegrityChannelTest {
    private Mac getMac(String fileName) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        final File hmacKeyFile = new File(fileName);
        final Key hmacKey = Keys.readSecretKey(hmacKeyFile);

        final Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(hmacKey);
        return hmac;
    }

    @Test
    public void testSendReceive_validKeyShouldWork() throws NoSuchAlgorithmException, InvalidKeyException, IOException, ChannelException {
        final Mac hmac = getMac("keys/hmac.key");

        final TestChannel testChannel = new TestChannel();
        final IntegrityChannel integrityChannel = new IntegrityChannel(hmac, testChannel);

        final String message = "!msg some plain text";

        // WHEN
        final Packet<byte[]> packet = new NetworkPacket<>();
        packet.pack(Utf8.decodeString(message));
        integrityChannel.send(packet);

        // THEN
        final Packet <byte[]> receivedPacket = integrityChannel.receive();
        final String receivedMessage = Utf8.encodeByteArray(receivedPacket.unpack());
        assertEquals(message, receivedMessage);
    }

    @Test(expected = ChannelIntegrityException.class)
    public void testSendReceive_invalidKeyShouldThrowChannelIntegrityException() throws NoSuchAlgorithmException, InvalidKeyException, IOException, ChannelException {
        final Mac hmacClient = getMac("keys/wrongHmac.key");
        final Mac hmacServer = getMac("keys/hmac.key");

        final TestChannel testChannel = new TestChannel();
        final IntegrityChannel integrityChannelClient = new IntegrityChannel(hmacClient, testChannel);
        final IntegrityChannel integrityChannelServer = new IntegrityChannel(hmacServer, testChannel);

        final String message = "!msg some plain text";

        // WHEN
        final Packet<byte[]> packet = new NetworkPacket<>();
        packet.pack(Utf8.decodeString(message));
        integrityChannelClient.send(packet);

        // THEN
        integrityChannelServer.receive();
    }

    @Test(expected = ChannelException.class)
    public void testSendReceive_shouldNotThrowChannelIntegrityExceptionWhenInnerReceiveFails() throws NoSuchAlgorithmException, InvalidKeyException, IOException, ChannelException {
        final Mac hmacClient = getMac("keys/wrongHmac.key");
        final Mac hmacServer = getMac("keys/hmac.key");

        final TestChannel testChannel = new TestChannel();
        final IntegrityChannel integrityChannelClient = new IntegrityChannel(hmacClient, testChannel);
        final IntegrityChannel integrityChannelServer = new IntegrityChannel(hmacServer, testChannel);

        final String message = "!msg some plain text";

        // WHEN
        final Packet<byte[]> packet = new NetworkPacket<>();
        packet.pack(Utf8.decodeString(message));
        integrityChannelClient.send(packet);

        testChannel.setExceptionOnReceive(true); // throw a normal ChannelException in base channel

        // THEN
        try {
            integrityChannelServer.receive();
        } catch (ChannelIntegrityException e) {
            fail("Should not throw a ChannelIntegrityException when inner receive throws a channel exception");
        }
    }
}
