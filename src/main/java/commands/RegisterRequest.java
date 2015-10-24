package commands;

import entities.PrivateAddress;
import states.State;
import states.StateException;
import states.StateResult;

public class RegisterRequest extends Request {
    private PrivateAddress privateAddress;

    public PrivateAddress getPrivateAddress() {
        return privateAddress;
    }

    public void setPrivateAddress(PrivateAddress privateAddress) {
        this.privateAddress = privateAddress;
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
