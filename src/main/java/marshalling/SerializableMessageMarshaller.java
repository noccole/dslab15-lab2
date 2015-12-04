package marshalling;

import messages.*;

import java.io.*;

public class SerializableMessageMarshaller implements MessageMarshaller {
    @Override
    public byte[] marshall(Message message) throws MarshallingException {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            final ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(message);
        } catch (IOException e) {
            throw new MarshallingException("could not serialize message: " + message, e);
        }
        return byteStream.toByteArray();
    }

    @Override
    public Message unmarshall(byte[] data) throws MarshallingException {
        try {
            final InputStream byteStream = new ByteArrayInputStream(data);
            final ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            return Message.class.cast(objectStream.readObject());
        } catch (IOException e) {
            final UnknownRequest unknownRequest = new UnknownRequest();
            unknownRequest.setRequestData(data);
            unknownRequest.setReason("could not deserialize message");
            return unknownRequest;
        } catch (ClassNotFoundException e) {
            final UnknownRequest unknownRequest = new UnknownRequest();
            unknownRequest.setRequestData(data);
            unknownRequest.setReason("received object was not of type Message");
            return unknownRequest;
        }
    }

    @Override
    public byte[] marshallErrorResponse(ErrorResponse response) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public ErrorResponse unmarshallErrorResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallListRequest(ListRequest request) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public ListRequest unmarshallListRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallListResponse(ListResponse response) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public ListResponse unmarshallListResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallLoginRequest(LoginRequest request) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public LoginRequest unmarshallLoginRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallLoginResponse(LoginResponse response) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public LoginResponse unmarshallLoginResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallLogoutRequest(LogoutRequest request) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public LogoutRequest unmarshallLogoutRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallLogoutResponse(LogoutResponse response) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public LogoutResponse unmarshallLogoutResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallLookupRequest(LookupRequest request) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public LookupRequest unmarshallLookupRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallLookupResponse(LookupResponse response) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public LookupResponse unmarshallLookupResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallRegisterRequest(RegisterRequest request) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public RegisterRequest unmarshallRegisterRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallRegisterResponse(RegisterResponse response) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public RegisterResponse unmarshallRegisterResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallSendMessageRequest(SendMessageRequest request) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public SendMessageRequest unmarshallSendMessageRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallSendMessageResponse(SendMessageResponse response) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public SendMessageResponse unmarshallSendMessageResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallSendPrivateMessageRequest(SendPrivateMessageRequest request) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public SendPrivateMessageRequest unmarshallSendPrivateMessageRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallSendPrivateMessageResponse(SendPrivateMessageResponse response) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public SendPrivateMessageResponse unmarshallSendPrivateMessageResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallUnknownRequest(UnknownRequest request) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public UnknownRequest unmarshallUnknownRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallTamperedRequest(TamperedRequest request) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public TamperedRequest unmarshallTamperedRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallTamperedResponse(TamperedResponse response) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public TamperedResponse unmarshallTamperedResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallExitEvent(ExitEvent event) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public ExitEvent unmarshallExitEvent(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallMessageEvent(MessageEvent event) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public MessageEvent unmarshallMessageEvent(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallUserPresenceChangedEvent(UserPresenceChangedEvent event) throws MarshallingException {
        return new byte[0];
    }

    @Override
    public UserPresenceChangedEvent unmarshallUserPresenceChangedEvent(byte[] data) throws MarshallingException {
        return null;
    }

	@Override
	public byte[] marshallAuthConfirmationResponse(
			AuthConfirmationResponse response) throws MarshallingException {
		return new byte[0];
	}

	@Override
	public AuthConfirmationResponse unmarshallAuthConfirmationResponse(
			byte[] data) throws MarshallingException {
		return null;
	}

	@Override
	public byte[] marshallAuthConfirmationRequest(
			AuthConfirmationRequest request) throws MarshallingException {
		return new byte[0];
	}

	@Override
	public AuthConfirmationRequest unmarshallAuthConfirmationRequest(byte[] data)
			throws MarshallingException {
		return null;
	}

	@Override
	public byte[] marshallAuthenticateResponse(AuthenticateResponse response)
			throws MarshallingException {
		return new byte[0];
	}

	@Override
	public AuthenticateResponse unmarshallAuthenticateResponse(byte[] data)
			throws MarshallingException {
		return null;
	}

	@Override
	public byte[] marshallAuthenticateRequest(AuthenticateRequest response)
			throws MarshallingException {
		return new byte[0];
	}

	@Override
	public AuthenticateRequest unmarshallAuthenticateRequest(byte[] data)
			throws MarshallingException {
		return null;
	}
}
