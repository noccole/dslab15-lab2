package messages;

public class TamperedResponse extends ErrorResponse {
    public TamperedResponse(Message request) {
        super(request);
    }
}
