package spec;

import channels.*;
import marshalling.Lab2ProtocolMarshaller;
import messages.AuthConfirmationRequest;
import messages.AuthenticateRequest;
import messages.AuthenticateResponse;
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

public class Stage2 {

    @Test
    public void testAuthenticateRequestMarshalling() throws ChannelException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        final TestChannel testChannel = new TestChannel();
        final MessageChannel messageChannel = new MessageChannel(new Lab2ProtocolMarshaller(), testChannel);
        
        final AuthenticateRequest request = new AuthenticateRequest(1);

        request.setClientChallenge("34512".getBytes());
        request.setUsername("bill.de");

        // WHEN
        final Packet<Message> packet = new NetworkPacket<>();
        packet.pack(request);
        messageChannel.send(packet);

        // THEN
        final Packet<byte[]> bytePacket = testChannel.getLastPacket();
        final byte[] bytes = bytePacket.unpack();
        final String message = Utf8.encodeByteArray(bytes);

        assertTrue(message.matches("!authenticate bill.de 34512")); // from stage 2 spec
    }
    
    @Test
    public void testAuthenticateResponseMarshalling() throws ChannelException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        final TestChannel testChannel = new TestChannel();
        final MessageChannel messageChannel = new MessageChannel(new Lab2ProtocolMarshaller(), testChannel);
        
        final AuthenticateResponse response = new AuthenticateResponse(1);

        response.setClientChallenge("34512".getBytes());
        response.setServerChallenge("54321".getBytes());
        response.setKey("abcde".getBytes());
        response.setIV("edcba".getBytes());

        // WHEN
        final Packet<Message> packet = new NetworkPacket<>();
        packet.pack(response);
        messageChannel.send(packet);

        // THEN
        final Packet<byte[]> bytePacket = testChannel.getLastPacket();
        final byte[] bytes = bytePacket.unpack();
        final String message = Utf8.encodeByteArray(bytes);

        assertTrue(message.matches("!ok 34512 54321 abcde edcba")); // from stage 2 spec
    }
    
    @Test
    public void testAuthConfirmationRequestMarshalling() throws ChannelException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        final TestChannel testChannel = new TestChannel();
        final MessageChannel messageChannel = new MessageChannel(new Lab2ProtocolMarshaller(), testChannel);
        
        final AuthConfirmationRequest request = new AuthConfirmationRequest(1);

        request.setServerChallenge("54321".getBytes());

        // WHEN
        final Packet<Message> packet = new NetworkPacket<>();
        packet.pack(request);
        messageChannel.send(packet);

        // THEN
        final Packet<byte[]> bytePacket = testChannel.getLastPacket();
        final byte[] bytes = bytePacket.unpack();
        final String message = Utf8.encodeByteArray(bytes);

        assertTrue(message.matches("54321")); // from stage 2 spec
    }
}
