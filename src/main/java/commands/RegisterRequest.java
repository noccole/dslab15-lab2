package commands;

import states.State;
import states.StateException;
import states.StateResult;

public class RegisterRequest extends Request {
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleRegisterRequest(this);
    }

    @Override
    public String toString() {
        return "register";
    }
}
