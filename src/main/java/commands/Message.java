package commands;

import states.State;
import states.StateException;
import states.StateResult;

import java.io.Serializable;

public interface Message extends Serializable {
    long getMessageId();

    StateResult applyTo(State state) throws StateException;
}
