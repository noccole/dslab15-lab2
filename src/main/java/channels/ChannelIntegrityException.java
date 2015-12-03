package channels;

public class ChannelIntegrityException extends ChannelException {
    private final byte[] bytes;

    public ChannelIntegrityException(String message, byte[] bytes) {
        super(message);
        this.bytes = bytes;
    }

    public ChannelIntegrityException(String message, Throwable cause, byte[] bytes) {
        super(message, cause);
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
