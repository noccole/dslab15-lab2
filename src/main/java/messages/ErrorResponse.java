package messages;

public class ErrorResponse extends Response {
    private String reason;

    public ErrorResponse(Message request) {
        super(request);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
