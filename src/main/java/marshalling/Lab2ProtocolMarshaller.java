package marshalling;

import messages.*;
import util.ArrayUtils;
import util.Utf8;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;

public class Lab2ProtocolMarshaller implements MessageMarshaller {
    private static final byte DELIMITER = (byte)32; // space

    private enum MessageType {
    	AUTHENTICATE_REQUEST("!authenticate"),
    	AUTHENTICATE_RESPONSE("!ok"),
    	AUTHCONFIRMATION_REQUEST(""),
    	AUTHCONFIRMATION_RESPONSE(""),
        ERROR_RESPONSE("!error"),
        EXIT_EVENT("!exit_event"),
        MESSAGE_EVENT("!msg_event"),
        USER_PRESENCE_CHANGED_EVENT("!upc_event"),
        LIST_REQUEST("!list"),
        LIST_RESPONSE("!list_resp"),
        LOGIN_REQUEST("!login"),
        LOGIN_RESPONSE("!login_resp"),
        LOGOUT_REQUEST("!logout"),
        LOGOUT_RESPONSE("!logout_resp"),
        LOOKUP_REQUEST("!lookup"),
        LOOKUP_RESPONSE("!lookup_resp"),
        REGISTER_REQUEST("!register"),
        REGISTER_RESPONSE("!register_resp"),
        SEND_MESSAGE_REQUEST("!send"),
        SEND_MESSAGE_RESPONSE("!send_resp"),
        SEND_PRIVATE_MESSAGE_REQUEST("!msg"),
        SEND_PRIVATE_MESSAGE_RESPONSE("!msg_resp"),
        UNKNOWN_REQUEST("!unknown"),
        TAMPERED_REQUEST("!tampered_req"),
        TAMPERED_RESPONSE("!tampered");

        private final String str;
        MessageType(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    private final Map<MessageType, Method> dispatcher = new HashMap<>();

    public Lab2ProtocolMarshaller() {
        try {
        	dispatcher.put(MessageType.AUTHENTICATE_RESPONSE, getClass().getMethod("unmarshallAuthenticateResponse", byte[].class));
        	dispatcher.put(MessageType.AUTHENTICATE_REQUEST, getClass().getMethod("unmarshallAuthenticateRequest", byte[].class));
        	dispatcher.put(MessageType.AUTHCONFIRMATION_RESPONSE, getClass().getMethod("unmarshallAuthConfirmationResponse", byte[].class));
        	dispatcher.put(MessageType.AUTHCONFIRMATION_REQUEST, getClass().getMethod("unmarshallAuthConfirmationRequest", byte[].class));
            dispatcher.put(MessageType.ERROR_RESPONSE, getClass().getMethod("unmarshallErrorResponse", byte[].class));
            dispatcher.put(MessageType.EXIT_EVENT, getClass().getMethod("unmarshallExitEvent", byte[].class));
            dispatcher.put(MessageType.MESSAGE_EVENT, getClass().getMethod("unmarshallMessageEvent", byte[].class));
            dispatcher.put(MessageType.USER_PRESENCE_CHANGED_EVENT, getClass().getMethod("unmarshallUserPresenceChangedEvent", byte[].class));
            dispatcher.put(MessageType.LIST_REQUEST, getClass().getMethod("unmarshallListRequest", byte[].class));
            dispatcher.put(MessageType.LIST_RESPONSE, getClass().getMethod("unmarshallListResponse", byte[].class));
            dispatcher.put(MessageType.LOGIN_REQUEST, getClass().getMethod("unmarshallLoginRequest", byte[].class));
            dispatcher.put(MessageType.LOGIN_RESPONSE, getClass().getMethod("unmarshallLoginResponse", byte[].class));
            dispatcher.put(MessageType.LOGOUT_REQUEST, getClass().getMethod("unmarshallLogoutRequest", byte[].class));
            dispatcher.put(MessageType.LOGOUT_RESPONSE, getClass().getMethod("unmarshallLogoutResponse", byte[].class));
            dispatcher.put(MessageType.LOOKUP_REQUEST, getClass().getMethod("unmarshallLookupRequest", byte[].class));
            dispatcher.put(MessageType.LOOKUP_RESPONSE, getClass().getMethod("unmarshallLookupResponse", byte[].class));
            dispatcher.put(MessageType.REGISTER_REQUEST, getClass().getMethod("unmarshallRegisterRequest", byte[].class));
            dispatcher.put(MessageType.REGISTER_RESPONSE, getClass().getMethod("unmarshallRegisterResponse", byte[].class));
            dispatcher.put(MessageType.SEND_MESSAGE_REQUEST, getClass().getMethod("unmarshallSendMessageRequest", byte[].class));
            dispatcher.put(MessageType.SEND_MESSAGE_RESPONSE, getClass().getMethod("unmarshallSendMessageResponse", byte[].class));
            dispatcher.put(MessageType.SEND_PRIVATE_MESSAGE_REQUEST, getClass().getMethod("unmarshallSendPrivateMessageRequest", byte[].class));
            dispatcher.put(MessageType.SEND_PRIVATE_MESSAGE_RESPONSE, getClass().getMethod("unmarshallSendPrivateMessageResponse", byte[].class));
            dispatcher.put(MessageType.UNKNOWN_REQUEST, getClass().getMethod("unmarshallUnknownRequest", byte[].class));
            dispatcher.put(MessageType.TAMPERED_REQUEST, getClass().getMethod("unmarshallTamperedRequest", byte[].class));
            dispatcher.put(MessageType.TAMPERED_RESPONSE, getClass().getMethod("unmarshallTamperedResponse", byte[].class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Override
    public byte[] marshall(Message message) throws MarshallingException { System.out.println("marsh");
        return message.marshall(this);
    }

    @Override
    public Message unmarshall(byte[] data) throws MarshallingException { System.out.println("unmarsh");
        if (data.length == 0) {
            final UnknownRequest unknownRequest = new UnknownRequest();
            unknownRequest.setRequestData(data);
            unknownRequest.setReason("could not deserialize message");
            return unknownRequest;
        }

        final List<byte[]> parts = ArrayUtils.split(data, DELIMITER, 2);
        assert !parts.isEmpty();

        final String messageType;
        try {
            final byte[] messageTypeBytes = parts.get(0);
            messageType = Utf8.encodeByteArray(messageTypeBytes);
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }

        Method method = null;
        for(MessageType m : dispatcher.keySet()) {
        	if(m.toString().equals(messageType.toString())) {
        		method = dispatcher.get(m);
        	}
        }

        if (method != null) {
            try {
                return Message.class.cast(method.invoke(getClass().newInstance(), new Object[]{data}));
            } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException | InstantiationException e) {
                throw new MarshallingException("Could not unmarshall the message", e);
            }
        } else {
            final UnknownRequest unknownRequest = new UnknownRequest();
            unknownRequest.setRequestData(data);
            unknownRequest.setReason("received object was not of type Message");
            return unknownRequest;
        }
    }

    @Override
    public byte[] marshallErrorResponse(ErrorResponse response) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d %s",
                    MessageType.ERROR_RESPONSE,
                    response.getMessageId(),
                    response.getReason()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public ErrorResponse unmarshallErrorResponse(byte[] data) throws MarshallingException {
        try {
            final Scanner scanner = new Scanner(Utf8.encodeByteArray(data));
            scanner.findInLine("^(\\s+)\\w(\\d+)\\w(\\s+)$");
            final MatchResult result = scanner.match();

            assert result.group(1).equals(MessageType.ERROR_RESPONSE);
            final long messageId = Long.parseLong(result.group(2));

            final ErrorResponse response = new ErrorResponse(messageId);
            response.setReason(result.group(3));
            return response;
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        } catch (IllegalStateException e) {
            throw new MarshallingException("Could not deserialize message", e);
        }
    }

    @Override
    public byte[] marshallListRequest(ListRequest request) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d",
                    MessageType.LIST_REQUEST,
                    request.getMessageId()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public ListRequest unmarshallListRequest(byte[] data) throws MarshallingException {
        try {
            final Scanner scanner = new Scanner(Utf8.encodeByteArray(data));
            scanner.findInLine("^(\\s+)\\w(\\d+)$");
            final MatchResult result = scanner.match();

            assert result.group(1).equals(MessageType.LIST_REQUEST);
            final long messageId = Long.parseLong(result.group(2));

            return new ListRequest(messageId);
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        } catch (IllegalStateException e) {
            throw new MarshallingException("Could not deserialize message", e);
        }
    }

    @Override
    public byte[] marshallListResponse(ListResponse response) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d %s",
                    MessageType.LIST_RESPONSE,
                    response.getMessageId(),
                    response.getUserList()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public ListResponse unmarshallListResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallLoginRequest(LoginRequest request) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d %s %s",
                    MessageType.LOGIN_REQUEST,
                    request.getMessageId(),
                    request.getUsername(),
                    request.getPassword()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public LoginRequest unmarshallLoginRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallLoginResponse(LoginResponse response) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d %s",
                    MessageType.LOGIN_RESPONSE,
                    response.getMessageId(),
                    response.getResponse()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public LoginResponse unmarshallLoginResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallLogoutRequest(LogoutRequest request) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d",
                    MessageType.LOGOUT_REQUEST,
                    request.getMessageId()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public LogoutRequest unmarshallLogoutRequest(byte[] data) throws MarshallingException {
        try {
            final Scanner scanner = new Scanner(Utf8.encodeByteArray(data));
            scanner.findInLine("^(\\s+)\\w(\\d+)$");
            final MatchResult result = scanner.match();

            assert result.group(1).equals(MessageType.LOGOUT_REQUEST);
            final long messageId = Long.parseLong(result.group(2));

            return new LogoutRequest(messageId);
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        } catch (IllegalStateException e) {
            throw new MarshallingException("Could not deserialize message", e);
        }
    }

    @Override
    public byte[] marshallLogoutResponse(LogoutResponse response) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d",
                    MessageType.LOGOUT_RESPONSE,
                    response.getMessageId()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public LogoutResponse unmarshallLogoutResponse(byte[] data) throws MarshallingException {
        try {
            final Scanner scanner = new Scanner(Utf8.encodeByteArray(data));
            scanner.findInLine("^(\\s+)\\w(\\d+)$");
            final MatchResult result = scanner.match();

            assert result.group(1).equals(MessageType.LOGOUT_RESPONSE);
            final long messageId = Long.parseLong(result.group(2));

            return new LogoutResponse(messageId);
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        } catch (IllegalStateException e) {
            throw new MarshallingException("Could not deserialize message", e);
        }
    }

    @Override
    public byte[] marshallLookupRequest(LookupRequest request) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d %s",
                    MessageType.LOOKUP_REQUEST,
                    request.getMessageId(),
                    request.getUsername()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public LookupRequest unmarshallLookupRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallLookupResponse(LookupResponse response) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d %s",
                    MessageType.LOOKUP_RESPONSE,
                    response.getMessageId(),
                    response.getPrivateAddress()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public LookupResponse unmarshallLookupResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallRegisterRequest(RegisterRequest request) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d %s",
                    MessageType.REGISTER_REQUEST,
                    request.getMessageId(),
                    request.getPrivateAddress()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public RegisterRequest unmarshallRegisterRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallRegisterResponse(RegisterResponse response) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d",
                    MessageType.REGISTER_RESPONSE,
                    response.getMessageId()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public RegisterResponse unmarshallRegisterResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallSendMessageRequest(SendMessageRequest request) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d %s",
                    MessageType.SEND_MESSAGE_REQUEST,
                    request.getMessageId(),
                    request.getMessage()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public SendMessageRequest unmarshallSendMessageRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallSendMessageResponse(SendMessageResponse response) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d",
                    MessageType.SEND_MESSAGE_RESPONSE,
                    response.getMessageId()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public SendMessageResponse unmarshallSendMessageResponse(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallSendPrivateMessageRequest(SendPrivateMessageRequest request) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d %s",
                    MessageType.SEND_PRIVATE_MESSAGE_REQUEST,
                    request.getMessageId(),
                    request.getMessage()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
    }

    @Override
    public SendPrivateMessageRequest unmarshallSendPrivateMessageRequest(byte[] data) throws MarshallingException {
        return null;
    }

    @Override
    public byte[] marshallSendPrivateMessageResponse(SendPrivateMessageResponse response) throws MarshallingException {
        try {
            return Utf8.decodeString(String.format("%s %d",
                    MessageType.SEND_PRIVATE_MESSAGE_RESPONSE,
                    response.getMessageId()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
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
			AuthConfirmationResponse response) throws MarshallingException { System.out.println("marsh Conf Res");
		try {
            return Utf8.decodeString(String.format("%s %d",
                    MessageType.AUTHCONFIRMATION_RESPONSE,
                    response.getMessageId()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
	}

	@Override
	public AuthConfirmationResponse unmarshallAuthConfirmationResponse(
			byte[] data) throws MarshallingException { System.out.println("unmarsh Conf Res");
		try {
            final Scanner scanner = new Scanner(Utf8.encodeByteArray(data));
            scanner.findInLine("^(\\s+)\\w(\\d+)$");
            final MatchResult result = scanner.match();

            assert result.group(1).equals(MessageType.AUTHCONFIRMATION_RESPONSE);
            final long messageId = Long.parseLong(result.group(2));

            AuthConfirmationResponse response =  new AuthConfirmationResponse(messageId);
            
            return response;
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        } catch (IllegalStateException e) {
            throw new MarshallingException("Could not deserialize message", e);
        }
	}

	@Override
	public byte[] marshallAuthConfirmationRequest(
			AuthConfirmationRequest request) throws MarshallingException { System.out.println("marsh Conf Req");
		try {
            return Utf8.decodeString(String.format("%s %s %s %d",
                    MessageType.AUTHCONFIRMATION_REQUEST,
                    request.getUsername(),
                    new String(request.getServerChallenge()),
                    request.getMessageId()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
	}

	@Override
	public AuthConfirmationRequest unmarshallAuthConfirmationRequest(byte[] data)
			throws MarshallingException { System.out.println("unmarsh Conf Req");
		try {
            final Scanner scanner = new Scanner(Utf8.encodeByteArray(data));
            scanner.findInLine("^(\\s+)\\w(\\d+)$");
            final MatchResult result = scanner.match();

            assert result.group(1).equals(MessageType.AUTHCONFIRMATION_REQUEST);
            final String username = new String(result.group(2).getBytes());
            final byte[] serverChallenge = result.group(3).getBytes();
            final long messageId = Long.parseLong(result.group(4));

            AuthConfirmationRequest request =  new AuthConfirmationRequest(messageId);
            request.setUsername(username);
            request.setServerChallenge(serverChallenge);
            
            return request;
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        } catch (IllegalStateException e) {
            throw new MarshallingException("Could not deserialize message", e);
        }
	}

	@Override
	public byte[] marshallAuthenticateResponse(AuthenticateResponse response)
			throws MarshallingException { System.out.println("marsh Auth Res");
		try {
            return Utf8.decodeString(String.format("%s %s %s %s %s %d",
                    MessageType.AUTHENTICATE_RESPONSE,
                    new String(response.getServerChallenge()),
                    new String(response.getClientChallenge()),
                    new String(response.getKey()),
                    new String(response.getIV()),
                    response.getMessageId()
                    
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
	}

	@Override
	public AuthenticateResponse unmarshallAuthenticateResponse(byte[] data)
			throws MarshallingException { System.out.println("unmarsh Auth Res");
		try {
            final Scanner scanner = new Scanner(Utf8.encodeByteArray(data));
            scanner.findInLine("^(\\s+)\\w(\\d+)$");
            final MatchResult result = scanner.match();

            assert result.group(1).equals(MessageType.AUTHENTICATE_RESPONSE);
            final byte[] serverChallenge = result.group(2).getBytes();
            final byte[] clientChallenge = result.group(3).getBytes();
            final byte[] key = result.group(4).getBytes();
            final byte[] iv = result.group(5).getBytes();
            final long messageId = Long.parseLong(result.group(6));

            AuthenticateResponse response =  new AuthenticateResponse(messageId);
            response.setServerChallenge(serverChallenge);
            response.setClientChallenge(clientChallenge);
            response.setKey(key);
            response.setIV(iv);
            
            return response;
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        } catch (IllegalStateException e) {
            throw new MarshallingException("Could not deserialize message", e);
        }
	}

	@Override
	public byte[] marshallAuthenticateRequest(AuthenticateRequest request)
			throws MarshallingException { System.out.println("marsh Auth Req");
		try {
            return Utf8.decodeString(String.format("%s %s %s %d",
                    MessageType.AUTHENTICATE_REQUEST,
                    request.getUsername(),
                    new String(request.getClientChallenge()),
                    request.getMessageId()
            ));
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
	}

	@Override
	public AuthenticateRequest unmarshallAuthenticateRequest(byte[] data)
			throws MarshallingException { System.out.println("unmarsh Auth Req");
		try {
            final Scanner scanner = new Scanner(Utf8.encodeByteArray(data));
            scanner.findInLine("^(\\s+)\\w(\\d+)$");
            final MatchResult result = scanner.match();

            assert result.group(1).equals(MessageType.AUTHENTICATE_REQUEST);
            final String username = new String(result.group(2).getBytes());
            final byte[] clientChallenge = result.group(3).getBytes();
            final long messageId = Long.parseLong(result.group(4));

            AuthenticateRequest request =  new AuthenticateRequest(messageId);
            request.setUsername(username);
            request.setClientChallenge(clientChallenge);
            
            return request;
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        } catch (IllegalStateException e) {
            throw new MarshallingException("Could not deserialize message", e);
        }
	}
}
