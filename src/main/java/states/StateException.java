package states;

public class StateException extends Exception {
    public StateException(String s) {
        super(s);
    }

    public StateException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
