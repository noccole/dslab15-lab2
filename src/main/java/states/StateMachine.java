package states;

import messages.Message;

public class StateMachine {
    private State state;

    public StateMachine(State initialState) {
        this.state = initialState;
    }

    public synchronized Message handleMessage(Message message) throws StateException {
        final StateResult stateResult = message.applyTo(state);

        if (state != stateResult.getNextState()) {
            // change current state
            try {
                state.onExited();
            } catch (StateException e) {
                System.err.println("Exception on leaving state '" + state + "': " + e);
            }

            state = stateResult.getNextState();

            try {
                state.onEntered();
            } catch (StateException e) {
                System.err.println("Exception on entering state '" + state + "': " + e);
            }
        }

        return stateResult.getResult();
    }
}
