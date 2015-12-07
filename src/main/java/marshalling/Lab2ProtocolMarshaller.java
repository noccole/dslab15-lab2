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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lab2ProtocolMarshaller implements MessageMarshaller {
    private static final byte DELIMITER = (byte)32; // space

    private enum MessageType {
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

    private final Map<String, Method> dispatcher = new HashMap<>();

    public Lab2ProtocolMarshaller() {
        registerUnmarshallingMethod(MessageType.ERROR_RESPONSE, "unmarshallErrorResponse");
        registerUnmarshallingMethod(MessageType.EXIT_EVENT, "unmarshallExitEvent");
        registerUnmarshallingMethod(MessageType.MESSAGE_EVENT, "unmarshallMessageEvent");
        registerUnmarshallingMethod(MessageType.USER_PRESENCE_CHANGED_EVENT, "unmarshallUserPresenceChangedEvent");
        registerUnmarshallingMethod(MessageType.LIST_REQUEST, "unmarshallListRequest");
        registerUnmarshallingMethod(MessageType.LIST_RESPONSE, "unmarshallListResponse");
        registerUnmarshallingMethod(MessageType.LOGIN_REQUEST, "unmarshallLoginRequest");
        registerUnmarshallingMethod(MessageType.LOGIN_RESPONSE, "unmarshallLoginResponse");
        registerUnmarshallingMethod(MessageType.LOGOUT_REQUEST, "unmarshallLogoutRequest");
        registerUnmarshallingMethod(MessageType.LOGOUT_RESPONSE, "unmarshallLogoutResponse");
        registerUnmarshallingMethod(MessageType.LOOKUP_REQUEST, "unmarshallLookupRequest");
        registerUnmarshallingMethod(MessageType.LOOKUP_RESPONSE, "unmarshallLookupResponse");
        registerUnmarshallingMethod(MessageType.REGISTER_REQUEST, "unmarshallRegisterRequest");
        registerUnmarshallingMethod(MessageType.REGISTER_RESPONSE, "unmarshallRegisterResponse");
        registerUnmarshallingMethod(MessageType.SEND_MESSAGE_REQUEST, "unmarshallSendMessageRequest");
        registerUnmarshallingMethod(MessageType.SEND_MESSAGE_RESPONSE, "unmarshallSendMessageResponse");
        registerUnmarshallingMethod(MessageType.SEND_PRIVATE_MESSAGE_REQUEST, "unmarshallSendPrivateMessageRequest");
        registerUnmarshallingMethod(MessageType.SEND_PRIVATE_MESSAGE_RESPONSE, "unmarshallSendPrivateMessageResponse");
        registerUnmarshallingMethod(MessageType.UNKNOWN_REQUEST, "unmarshallUnknownRequest");
        registerUnmarshallingMethod(MessageType.TAMPERED_REQUEST, "unmarshallTamperedRequest");
        registerUnmarshallingMethod(MessageType.TAMPERED_RESPONSE, "unmarshallTamperedResponse");
    }

    private void registerUnmarshallingMethod(MessageType messageType, String methodName) {
        try {
            dispatcher.put(messageType.toString(), getClass().getMethod(methodName, byte[].class));
        } catch (NoSuchMethodException e) {
            System.err.println("Could not register unmarshalling method '" + methodName + "'!");
            e.printStackTrace();
        }
    }

    @Override
    public byte[] marshall(Message message) throws MarshallingException {
        return message.marshall(this);
    }

    @Override
    public Message unmarshall(byte[] data) throws MarshallingException {
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

        final Method method = dispatcher.get(messageType);
        if (method != null) {
            try {
                return Message.class.cast(method.invoke(this, data));
            } catch (IllegalAccessException | InvocationTargetException e) {
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
            final Pattern pattern = Pattern.compile("^(?<type>\\S+)\\s(?<id>\\d+)\\s(?<reason>.+)$");
            final Matcher matcher = pattern.matcher(Utf8.encodeByteArray(data));
            if (!matcher.matches()) {
                throw new MarshallingException("Could not deserialize message");
            }

            assert matcher.group("type").equals(MessageType.ERROR_RESPONSE.toString());
            final long messageId = Long.parseLong(matcher.group("id"));

            final ErrorResponse response = new ErrorResponse(messageId);
            response.setReason(matcher.group("reason"));
            return response;
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
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
        return null;
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
        try {
            final Pattern pattern = Pattern.compile("^(?<type>\\S+)\\s(?<id>\\d+)\\s(?<username>\\S+)\\s(?<password>.+)$");
            final Matcher matcher = pattern.matcher(Utf8.encodeByteArray(data));
            if (!matcher.matches()) {
                throw new MarshallingException("Could not deserialize message");
            }

            assert matcher.group("type").equals(MessageType.LOGIN_REQUEST.toString());
            final long messageId = Long.parseLong(matcher.group("id"));

            final LoginRequest request = new LoginRequest(messageId);
            request.setUsername(matcher.group("username"));
            request.setPassword(matcher.group("password"));
            return request;
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
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
        try {
            final Pattern pattern = Pattern.compile("^(?<type>\\S+)\\s(?<id>\\d+)\\s(?<response>.+)$");
            final Matcher matcher = pattern.matcher(Utf8.encodeByteArray(data));
            if (!matcher.matches()) {
                throw new MarshallingException("Could not deserialize message");
            }

            assert matcher.group("type").equals(MessageType.LOGIN_RESPONSE.toString());
            final long messageId = Long.parseLong(matcher.group("id"));

            final LoginResponse response = new LoginResponse(messageId);
            response.setResponse(LoginResponse.ResponseCode.valueOf(matcher.group("response")));
            return response;
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
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
            final Pattern pattern = Pattern.compile("^(?<type>\\S+)\\s(?<id>\\d+)");
            final Matcher matcher = pattern.matcher(Utf8.encodeByteArray(data));
            if (!matcher.matches()) {
                throw new MarshallingException("Could not deserialize message");
            }

            assert matcher.group("type").equals(MessageType.LOGOUT_REQUEST.toString());
            final long messageId = Long.parseLong(matcher.group("id"));

            return new LogoutRequest(messageId);
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
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
            final Pattern pattern = Pattern.compile("^(?<type>\\S+)\\s(?<id>\\d+)$");
            final Matcher matcher = pattern.matcher(Utf8.encodeByteArray(data));
            if (!matcher.matches()) {
                throw new MarshallingException("Could not deserialize message");
            }

            assert matcher.group("type").equals(MessageType.LOGOUT_RESPONSE.toString());
            final long messageId = Long.parseLong(matcher.group("id"));

            return new LogoutResponse(messageId);
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
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
        try {
            final Pattern pattern = Pattern.compile("^(?<type>\\S+)\\s(?<id>\\d+)\\s(?<message>.+)$");
            final Matcher matcher = pattern.matcher(Utf8.encodeByteArray(data));
            if (!matcher.matches()) {
                throw new MarshallingException("Could not deserialize message");
            }

            assert matcher.group("type").equals(MessageType.SEND_MESSAGE_REQUEST.toString());
            final long messageId = Long.parseLong(matcher.group("id"));

            final SendMessageRequest request = new SendMessageRequest(messageId);
            request.setMessage(matcher.group("message"));
            return request;
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
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
        try {
            final Pattern pattern = Pattern.compile("^(?<type>\\S+)\\s(?<id>\\d+)$");
            final Matcher matcher = pattern.matcher(Utf8.encodeByteArray(data));
            if (!matcher.matches()) {
                throw new MarshallingException("Could not deserialize message");
            }

            assert matcher.group("type").equals(MessageType.SEND_MESSAGE_RESPONSE.toString());
            final long messageId = Long.parseLong(matcher.group("id"));

            return new SendMessageResponse(messageId);
        } catch (UnsupportedEncodingException e) {
            throw new MarshallingException("Unsupported encoding", e);
        }
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
}
