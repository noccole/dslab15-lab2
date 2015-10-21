package commands;

public class ErrorResponse extends Response {
    private String reason;

    public ErrorResponse(Request request) {
        super(request);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
