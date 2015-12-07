package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class SendPrivateMessageRequest extends Request {
    private String sender;
    private String message;

    public SendPrivateMessageRequest() {
        super();
    }

    public SendPrivateMessageRequest(long messageId) {
        super(messageId);
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleSendPrivateMessageRequest(this);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallSendPrivateMessageRequest(this);
    }

    @Override
    public String toString() {
        return "send private message";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SendPrivateMessageRequest)) return false;
        if (!super.equals(o)) return false;

        SendPrivateMessageRequest that = (SendPrivateMessageRequest) o;

        if (sender != null ? !sender.equals(that.sender) : that.sender != null) return false;
        return !(message != null ? !message.equals(that.message) : that.message != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
