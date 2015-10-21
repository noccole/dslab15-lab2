package commands;

public class SendPrivateMessageResponse extends Response {
    public SendPrivateMessageResponse(SendPrivateMessageRequest request) {
        super(request);
    }

    @Override
    public String toString() {
        return "send private message result";
    }
}
