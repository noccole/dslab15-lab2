package messages;

public class LoginResponse extends Response {
    private boolean success;

    public LoginResponse(LoginRequest request) {
        super(request);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "login result";
    }
}
