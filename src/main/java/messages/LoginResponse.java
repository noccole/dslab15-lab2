package messages;

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
    public String toString() {
        return "login result";
    }
}
