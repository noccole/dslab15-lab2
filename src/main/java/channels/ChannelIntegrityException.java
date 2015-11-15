package channels;

public class ChannelIntegrityException extends ChannelException {
    public ChannelIntegrityException(String message) {
        super(message);
    }

    public ChannelIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }
}
