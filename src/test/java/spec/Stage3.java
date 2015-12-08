package spec;

import channels.*;
import marshalling.Lab2ProtocolMarshaller;
import messages.Message;
import messages.SendPrivateMessageRequest;
import org.junit.Test;
import util.Keys;
import util.Utf8;

import javax.crypto.Mac;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertTrue;

public class Stage3 {
    private Mac getMac(String fileName) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        final File hmacKeyFile = new File(fileName);
        final Key hmacKey = Keys.readSecretKey(hmacKeyFile);

        final Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(hmacKey);
        return hmac;
    }

    @Test
    public void verifyHashedMessageForm() throws ChannelException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        final Mac hmac = getMac("keys/hmac.key");

        final TestChannel testChannel = new TestChannel();
        final IntegrityChannel integrityChannel = new IntegrityChannel(hmac, testChannel);
        final MessageChannel messageChannel = new MessageChannel(new Lab2ProtocolMarshaller(), integrityChannel);

        final SendPrivateMessageRequest request = new SendPrivateMessageRequest();
        request.setSender("alice@vienna.at");
        request.setMessage("this is a test message");

        // WHEN
        final Packet<Message> packet = new NetworkPacket<>();
        packet.pack(request);
        messageChannel.send(packet);

        // THEN
        final Packet<byte[]> bytePacket = testChannel.getLastPacket();
        final byte[] bytes = bytePacket.unpack();
        final String message = Utf8.encodeByteArray(bytes);

        assertTrue(message.matches("[a-zA-Z0-9/+]{43}=[\\s[^\\s]]+")); // from stage 3 spec
    }
}
