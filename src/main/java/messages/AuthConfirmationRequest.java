package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class AuthConfirmationRequest extends Request {
	private String username;
    private byte[] serverChallenge;
    
    public AuthConfirmationRequest(long messageId) {
    	super(messageId);
    }
    
    public String getUsername() {
        return username;
    }

    public byte[] getServerChallenge() {
    	return serverChallenge;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setServerChallenge(byte[] serverChallenge) {
    	this.serverChallenge = serverChallenge;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleAuthConfirmationRequest(this);
    }

    @Override
    public String toString() {
        return "authconfirmation";
    }

	@Override
	public byte[] marshall(MessageMarshaller marshaller)
			throws MarshallingException {
		return marshaller.marshallAuthConfirmationRequest(this);
	}
}
