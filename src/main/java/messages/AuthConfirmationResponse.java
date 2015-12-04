package messages;

import entities.PrivateAddress;

import java.util.Collection;

import javax.crypto.SecretKey;

import marshalling.MarshallingException;
import marshalling.MessageMarshaller;
import messages.LoginResponse.ResponseCode;

public class AuthConfirmationResponse extends Response {
    public AuthConfirmationResponse(AuthConfirmationRequest request) {
        super(request);
    }

    @Override
    public String toString() {
        return "authconfirmation result";
    }

	@Override
	public byte[] marshall(MessageMarshaller marshaller)
			throws MarshallingException {
		return marshaller.marshallAuthConfirmationResponse(this);
	}
}
