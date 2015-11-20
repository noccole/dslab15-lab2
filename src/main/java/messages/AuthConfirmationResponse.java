package messages;

import entities.PrivateAddress;

import java.util.Collection;

import javax.crypto.SecretKey;

import messages.LoginResponse.ResponseCode;

public class AuthConfirmationResponse extends Response {
    public AuthConfirmationResponse(AuthConfirmationRequest request) {
        super(request);
    }

    @Override
    public String toString() {
        return "authconfirmation result";
    }
}
