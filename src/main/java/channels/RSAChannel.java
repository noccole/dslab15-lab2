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

public class RSAChannel extends ChannelDecorator<byte[]> {
    private static final Logger LOGGER = Logger.getAnonymousLogger();
    
    private Key privateKey;
    private Key publicKey;
    private String receiveAlgorithm;
    private String sendAlgorithm;
    private boolean receiveAES = false;
    private boolean sendAES = false;
    private byte[] iv;
    
    private Channel channel;

    public RSAChannel(Channel channel) {
    	super(channel);
    	this.channel = channel;
    }
    
    public void setPrivateKey(Key privateKey) {
    	this.privateKey = privateKey;
    }
    
    public void setPublicKey(Key publicKey) {
    	this.publicKey = publicKey;
    }
    
    public void setReceiveAlgorithm(String receiveAlgorithm) {
    	this.receiveAlgorithm = receiveAlgorithm;
    }
    
    public void setSendAlgorithm(String sendAlgorithm) {
    	this.sendAlgorithm = sendAlgorithm;
    }
    
    public void setIV(byte[] iv) {
    	this.iv = iv;
    }
    
    public void setReceiveAES(boolean receiveAES) {
    	this.receiveAES = receiveAES;
    }
    
    public void setSendAES(boolean sendAES) {
    	this.sendAES = sendAES;
    }

    @Override
    public void send(Packet packet) throws ChannelException { 
    	Packet<byte[]> resPacket = new NetworkPacket<byte[]>();
		try {
			// make sure to use the right ALGORITHM for what you want to do (see text)
	    	Cipher cipher;
			cipher = Cipher.getInstance(sendAlgorithm);
			// MODE is the encryption/decryption mode
	    	// KEY is either a private, public or secret key
	    	// IV is an init vector, needed for AES
			if(sendAES)
				cipher.init(Cipher.ENCRYPT_MODE, publicKey, new IvParameterSpec(iv));
			else
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
	    	resPacket.pack(cipher.doFinal((byte[]) packet.unpack()));
		} catch (ArrayIndexOutOfBoundsException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			System.err.println("Problem with sending");
			e.printStackTrace();
		}

        channel.send(resPacket);
    }

    @Override
    public Packet receive() throws ChannelException {
    	Packet<byte[]> recPacket = channel.receive();
    	Packet<byte[]> resPacket = new NetworkPacket<byte[]>();

    	try {
			// make sure to use the right ALGORITHM for what you want to do (see text)
	    	Cipher cipher;
			cipher = Cipher.getInstance(receiveAlgorithm);
			// MODE is the encryption/decryption mode
	    	// KEY is either a private, public or secret key
	    	// IV is an init vector, needed for AES
			if(receiveAES)
				cipher.init(Cipher.DECRYPT_MODE, privateKey, new IvParameterSpec(iv));
			else
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
	    	resPacket.pack(cipher.doFinal((byte[]) recPacket.unpack()));
		} catch (ArrayIndexOutOfBoundsException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			System.err.println("Problem with receiving");
			e.printStackTrace();
		}
    	
        return resPacket;
    }
    
    public Channel getChannel() {
    	return channel.getChannel();
    }
}
