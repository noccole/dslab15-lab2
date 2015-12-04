package messages;

import entities.PrivateAddress;

import java.util.Collection;

import javax.crypto.SecretKey;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import messages.LoginResponse.ResponseCode;

public class AuthenticateResponse extends Response {
    private byte[] clientChallenge;
    private byte[] serverChallenge;
    private byte[] key;
    private byte[] iv;
    private byte[] code;
    
    /*public enum ResponseCode {
        Success,
        OkSent,
        UnknownUser,
        UserAlreadyAuthenticated
    }*/

    //private ResponseCode response;

    public AuthenticateResponse(AuthenticateRequest request) {
        super(request);
    }

    public byte[] getClientChallenge() {
        return clientChallenge;
    }

    public void setServerChallenge(byte[] serverChallenge) {
        this.serverChallenge = serverChallenge;
    }
    
    public byte[] getServerChallenge() {
        return serverChallenge;
    }

    public void setClientChallenge(byte[] clientChallenge) {
        this.clientChallenge = clientChallenge;
    }
   
    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }
    
    public byte[] getIV() {
        return iv;
    }

    public void setIV(byte[] iv) {
        this.iv = iv;
    }
    
    /*public void setResponse(ResponseCode response) {
        this.response = response;
    }*/
    
    public void setResponseCode(String code) {
    	this.code = code.getBytes();
    }

    @Override
    public String toString() {
        return "authenticate result";
    }

	@Override
	public byte[] marshall(MessageMarshaller marshaller)
			throws MarshallingException {
		return marshaller.marshallAuthenticateResponse(this);
	}
}
