package commands;

public class RegisterResponse extends Response {
    public RegisterResponse(RegisterRequest request) {
        super(request);
    }

    @Override
    public String toString() {
        return "register result";
    }
}
