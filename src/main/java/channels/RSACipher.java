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

public class RSACipher implements CipherMode {
    private static final Logger LOGGER = Logger.getAnonymousLogger();
    
    private Key privateKey;
    private Key publicKey;

    public RSACipher() {
    }
    
    public void setPrivateKey(Key privateKey) {
    	this.privateKey = privateKey;
    }
    
    public void setPublicKey(Key publicKey) {
    	this.publicKey = publicKey;
    }

    @Override
    public Packet encrypt(Packet packet) {
    	Packet<byte[]> resPacket = new NetworkPacket<byte[]>();
		try {
			// make sure to use the right ALGORITHM for what you want to do (see text)
	    	Cipher cipher;
			cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			// MODE is the encryption/decryption mode
	    	// KEY is either a private, public or secret key
	    	// IV is an init vector, needed for AES
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
	    	resPacket.pack(cipher.doFinal((byte[]) packet.unpack()));
		} catch (ArrayIndexOutOfBoundsException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			System.err.println("Problem with sending");
			e.printStackTrace();
		}

        return resPacket;
    }

    @Override
    public Packet decrypt(Packet packet) {
    	Packet<byte[]> resPacket = new NetworkPacket<byte[]>();

    	try {
			// make sure to use the right ALGORITHM for what you want to do (see text)
	    	Cipher cipher;
			cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			// MODE is the encryption/decryption mode
	    	// KEY is either a private, public or secret key
	    	// IV is an init vector, needed for AES
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
	    	resPacket.pack(cipher.doFinal((byte[]) packet.unpack()));
		} catch (ArrayIndexOutOfBoundsException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			System.err.println("Problem with receiving");
			e.printStackTrace();
		}
    	
        return resPacket;
    }
}
