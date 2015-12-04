package messages;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import states.State;
import states.StateException;
import states.StateResult;

public class AuthenticateRequest extends Request {
    private String username;
    private byte[] clientChallenge;

    public String getUsername() {
        return username;
    }
    
    public byte[] getClientChallenge() {
    	return clientChallenge;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setClientChallenge(byte[] clientChallenge) {
    	this.clientChallenge = clientChallenge;
    }

    @Override
    public StateResult applyTo(State state) throws StateException {
        return state.handleAuthenticateRequest(this);
    }

    @Override
    public String toString() {
        return "authenticate";
    }

	@Override
	public byte[] marshall(MessageMarshaller marshaller)
			throws MarshallingException {
		return marshaller.marshallAuthenticateRequest(this);
	}
}
