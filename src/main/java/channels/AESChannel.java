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

public class AESChannel extends ChannelDecorator<byte[]> {
    private static final Logger LOGGER = Logger.getAnonymousLogger();
    
    private Key key;
    private byte[] iv;
    
    private Channel channel;

    public AESChannel(Channel channel) {
    	super(channel);
    	this.channel = channel;
    }
    
    public void setKey(Key key) {
    	this.key = key;
    }
    
    public void setIV(byte[] iv) {
    	this.iv = iv;
    }

    @Override
    public void send(Packet packet) throws ChannelException { System.out.println("Sending with AES");
    	Packet<byte[]> resPacket = new NetworkPacket<byte[]>();
		try {
			// make sure to use the right ALGORITHM for what you want to do (see text)
	    	Cipher cipher;
			cipher = Cipher.getInstance("AES/CTR/NoPadding");
			// MODE is the encryption/decryption mode
	    	// KEY is either a private, public or secret key
	    	// IV is an init vector, needed for AES
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
	    	resPacket.pack(cipher.doFinal((byte[]) packet.unpack()));
		} catch (ArrayIndexOutOfBoundsException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			System.err.println("Problem with sending AES");
			e.printStackTrace();
		}

        channel.send(resPacket);
    }

    @Override
    public Packet receive() throws ChannelException { System.out.println("Receiving with AES");
    	Packet<byte[]> recPacket = channel.receive();
    	Packet<byte[]> resPacket = new NetworkPacket<byte[]>();

    	try {
			// make sure to use the right ALGORITHM for what you want to do (see text)
	    	Cipher cipher;
			cipher = Cipher.getInstance("AES/CTR/NoPadding");
			// MODE is the encryption/decryption mode
	    	// KEY is either a private, public or secret key
	    	// IV is an init vector, needed for AES
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
	    	resPacket.pack(cipher.doFinal((byte[]) recPacket.unpack()));
		} catch (ArrayIndexOutOfBoundsException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			System.err.println("Problem with receiving AES");
			e.printStackTrace();
		}
    	
        return resPacket;
    }
    
    public Channel getChannel() {
    	return channel.getChannel();
    }
}
