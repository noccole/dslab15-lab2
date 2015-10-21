package commands;

import states.State;
import states.StateException;

public class LoginRequest extends Request {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleLoginRequest(this);
    }

    @Override
    public String toString() {
        return "login";
    }
}
