package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

public class LoginResponse extends Response {
    public enum ResponseCode {
        Success,
        WrongPassword,
        UnknownUser,
        UserAlreadyLoggedIn
    }

    private ResponseCode response;

    public LoginResponse(LoginRequest request) {
        super(request);
    }

    public LoginResponse(long messageId) {
        super(messageId);
    }

    public ResponseCode getResponse() {
        return response;
    }

    public void setResponse(ResponseCode response) {
        this.response = response;
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallLoginResponse(this);
    }

    @Override
    public String toString() {
        return "login result";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginResponse)) return false;
        if (!super.equals(o)) return false;

        LoginResponse that = (LoginResponse) o;

        return response == that.response;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (response != null ? response.hashCode() : 0);
        return result;
    }
}
