package channels;

import org.bouncycastle.util.encoders.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

public class SecureChannel extends ChannelDecorator<byte[]> {
    private static final Logger LOGGER = Logger.getAnonymousLogger();
    
    private CipherMode receiveCipherMode;
    private CipherMode sendCipherMode;
    
    private Channel channel;

    public SecureChannel(Channel channel) {
    	super(channel);
    	this.channel = channel;
    }
    
    public void setSendCipherMode(CipherMode cipherMode) {
    	this.sendCipherMode = cipherMode;
    }
    
    public void setReceiveCipherMode(CipherMode cipherMode) {
    	this.receiveCipherMode = cipherMode;
    }

    @Override
    public void send(Packet packet) throws ChannelException { 
        channel.send(sendCipherMode.encrypt(packet));
    }

    @Override
    public Packet receive() throws ChannelException {
    	Packet<byte[]> packet = channel.receive();
    	
        return receiveCipherMode.decrypt(packet);
    }
    
    public Channel getChannel() {
    	return channel.getChannel();
    }
}
