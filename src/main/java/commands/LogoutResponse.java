package commands;

public class LogoutResponse extends Response {
    public LogoutResponse(LogoutRequest request) {
        super(request);
    }

    @Override
    public String toString() {
        return "logout result";
    }
}
