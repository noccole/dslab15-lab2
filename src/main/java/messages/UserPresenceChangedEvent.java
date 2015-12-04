package messages;

import entities.User;
import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class UserPresenceChangedEvent extends Event {
    private String username;
    private User.Presence presence;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User.Presence getPresence() {
        return presence;
    }

    public void setPresence(User.Presence presence) {
        this.presence = presence;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleUserStateChangedEvent(this);
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallUserPresenceChangedEvent(this);
    }

    @Override
    public String toString() {
        return "user presence changed event";
    }
}
