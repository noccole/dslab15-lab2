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

public class AESCipher implements CipherMode {
    private static final Logger LOGGER = Logger.getAnonymousLogger();
    
    private Key key;
    private byte[] iv;
    
    private Channel channel;

    public AESCipher() {
    }
    
    public void setKey(Key key) {
    	this.key = key;
    }
    
    public void setIV(byte[] iv) {
    	this.iv = iv;
    }

    @Override
    public Packet encrypt(Packet packet) { System.out.println("Encrypting with AES");
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

        return resPacket;
    }

    @Override
    public Packet decrypt(Packet packet) { System.out.println("Decrypting with AES");
    	Packet<byte[]> resPacket = new NetworkPacket<byte[]>();

    	try {
			// make sure to use the right ALGORITHM for what you want to do (see text)
	    	Cipher cipher;
			cipher = Cipher.getInstance("AES/CTR/NoPadding");
			// MODE is the encryption/decryption mode
	    	// KEY is either a private, public or secret key
	    	// IV is an init vector, needed for AES
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
	    	resPacket.pack(cipher.doFinal((byte[]) packet.unpack()));
		} catch (ArrayIndexOutOfBoundsException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			System.err.println("Problem with receiving AES");
			e.printStackTrace();
		}
    	
        return resPacket;
    }
}
