package messages;

import states.State;
import states.StateException;
import states.StateResult;

import java.io.Serializable;

public interface Message extends Serializable {
    /**
     * @return The unique message id of the current message
     */
    long getMessageId();

    /**
     * Apply the message to the given state
     *
     * @param state State which should handle the current message
     * @return State result which includes the next state and an optional result
     * @throws StateException
     */
    StateResult applyTo(State state) throws StateException;
}
