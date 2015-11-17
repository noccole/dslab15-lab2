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
}
