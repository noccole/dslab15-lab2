package channels;

public class ChannelException extends Exception {
    public ChannelException(String s) {
        super(s);
    }

    public ChannelException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
