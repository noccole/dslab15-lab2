package commands;

public class SendMessageResponse extends Response {
    public SendMessageResponse(SendMessageRequest request) {
        super(request);
    }

    @Override
    public String toString() {
        return "send message result";
    }
}
