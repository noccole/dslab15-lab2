package channels;

public interface CipherMode {
	Packet encrypt(Packet packet);
	
	Packet decrypt(Packet packet);
}
