package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class ExitEvent extends Event {
    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleExitEvent(this);
    }

    @Override
    public String toString() {
        return "exit event";
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallExitEvent(this);
    }
}
