package marshalling;

import entities.PrivateAddress;
import entities.User;
import messages.*;
import org.junit.Test;
import util.Utf8;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public abstract class MarshallingTest {
    private final MessageMarshaller marshaller;

    public MarshallingTest(MessageMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Test
    public void testErrorResponse() throws MarshallingException {
        // PREPARE
        final LoginRequest request = new LoginRequest();
        request.setUsername("bill@test.at");
        request.setPassword("t # as asdf123");

        final ErrorResponse response = new ErrorResponse(request);
        response.setReason("only for testing");

        final byte[] data = marshaller.marshall(response);

        // WHEN
        final ErrorResponse unmarshalledResponse = ErrorResponse.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(response, unmarshalledResponse);
    }

    @Test
    public void testUnknownRequest() throws MarshallingException, UnsupportedEncodingException {
        // PREPARE
        final UnknownRequest request = new UnknownRequest();
        request.setReason("a unknown request");
        request.setRequestData(Utf8.decodeString("some byte data"));

        final byte[] data = marshaller.marshall(request);

        // WHEN
        final UnknownRequest unmarshalledRequest = UnknownRequest.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(request, unmarshalledRequest);
    }

    @Test
    public void testExitEvent() throws MarshallingException {
        // PREPARE
        final ExitEvent event = new ExitEvent();

        final byte[] data = marshaller.marshall(event);

        // WHEN
        final ExitEvent unmarshalledEvent = ExitEvent.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(event, unmarshalledEvent);
    }

    @Test
    public void testMessageEvent() throws MarshallingException {
        // PREPARE
        final MessageEvent event = new MessageEvent();
        event.setUsername("tester@home.at");
        event.setMessage("test message");

        final byte[] data = marshaller.marshall(event);

        // WHEN
        final MessageEvent unmarshalledEvent = MessageEvent.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(event, unmarshalledEvent);
    }

    @Test
    public void testUserPresenceChangedEvent() throws MarshallingException {
        // PREPARE
        final UserPresenceChangedEvent event = new UserPresenceChangedEvent();
        event.setUsername("tester@home.at");
        event.setPresence(User.Presence.Available);

        final byte[] data = marshaller.marshall(event);

        // WHEN
        final UserPresenceChangedEvent unmarshalledEvent = UserPresenceChangedEvent.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(event, unmarshalledEvent);
    }

    @Test
    public void testListRequest() throws MarshallingException {
        // PREPARE
        final ListRequest request = new ListRequest();

        final byte[] data = marshaller.marshall(request);

        // WHEN
        final ListRequest unmarshalledRequest = ListRequest.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(request, unmarshalledRequest);
    }

    @Test
    public void testListResponse() throws MarshallingException {
        // PREPARE
        final ListRequest request = new ListRequest();

        final Map<String, User.Presence> users = new HashMap<>();
        users.put("alice@vienna.at", User.Presence.Available);
        users.put("lee@ibk.at", User.Presence.Offline);

        final ListResponse response = new ListResponse(request);
        response.setUserList(users);

        final byte[] data = marshaller.marshall(response);

        // WHEN
        final ListResponse unmarshalledResponse = ListResponse.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(response, unmarshalledResponse);
    }

    @Test
    public void testLoginRequest() throws MarshallingException {
        // PREPARE
        final LoginRequest request = new LoginRequest();
        request.setUsername("bill@test.at");
        request.setPassword("t # as asdf123");

        final byte[] data = marshaller.marshall(request);

        // WHEN
        final LoginRequest unmarshalledRequest = LoginRequest.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(request, unmarshalledRequest);
    }

    @Test
    public void testLoginResponse() throws MarshallingException {
        // PREPARE
        final LoginRequest request = new LoginRequest();
        request.setUsername("bill@test.at");
        request.setPassword("t # as asdf123");

        final LoginResponse response = new LoginResponse(request);
        response.setResponse(LoginResponse.ResponseCode.UserAlreadyLoggedIn);

        final byte[] data = marshaller.marshall(response);

        // WHEN
        final LoginResponse unmarshalledResponse = LoginResponse.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(response, unmarshalledResponse);
    }

    @Test
    public void testLogoutRequest() throws MarshallingException {
        // PREPARE
        final LogoutRequest request = new LogoutRequest();

        final byte[] data = marshaller.marshall(request);

        // WHEN
        final LogoutRequest unmarshalledRequest = LogoutRequest.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(request, unmarshalledRequest);
    }

    @Test
    public void testLogoutResponse() throws MarshallingException {
        // PREPARE
        final LogoutRequest request = new LogoutRequest();

        final LogoutResponse response = new LogoutResponse(request);

        final byte[] data = marshaller.marshall(response);

        // WHEN
        final LogoutResponse unmarshalledResponse = LogoutResponse.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(response, unmarshalledResponse);
    }

    @Test
    public void testLookupRequest() throws MarshallingException {
        // PREPARE
        final LookupRequest request = new LookupRequest();
        request.setUsername("alice@vienna.at");

        final byte[] data = marshaller.marshall(request);

        // WHEN
        final LookupRequest unmarshalledRequest = LookupRequest.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(request, unmarshalledRequest);
    }

    @Test
    public void testLookupResponse() throws MarshallingException {
        // PREPARE
        final LookupRequest request = new LookupRequest();
        request.setUsername("alice@vienna.at");

        final PrivateAddress privateAddress = new PrivateAddress();
        privateAddress.setHostname("localhost");
        privateAddress.setPort(12345);

        final LookupResponse response = new LookupResponse(request);
        response.setPrivateAddress(privateAddress);

        final byte[] data = marshaller.marshall(response);

        // WHEN
        final LookupResponse unmarshalledResponse = LookupResponse.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(response, unmarshalledResponse);
    }

    @Test
    public void testRegisterRequest() throws MarshallingException {
        // PREPARE
        final PrivateAddress privateAddress = new PrivateAddress();
        privateAddress.setHostname("localhost");
        privateAddress.setPort(12346);

        final RegisterRequest request = new RegisterRequest();
        request.setPrivateAddress(privateAddress);

        final byte[] data = marshaller.marshall(request);

        // WHEN
        final RegisterRequest unmarshalledRequest = RegisterRequest.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(request, unmarshalledRequest);
    }

    @Test
    public void testRegisterResponse() throws MarshallingException {
        // PREPARE
        final PrivateAddress privateAddress = new PrivateAddress();
        privateAddress.setHostname("localhost");
        privateAddress.setPort(12346);

        final RegisterRequest request = new RegisterRequest();
        request.setPrivateAddress(privateAddress);

        final RegisterResponse response = new RegisterResponse(request);

        final byte[] data = marshaller.marshall(response);

        // WHEN
        final RegisterResponse unmarshalledResponse = RegisterResponse.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(response, unmarshalledResponse);
    }

    @Test
    public void testSendMessageRequest() throws MarshallingException {
        // PREPARE
        final SendMessageRequest request = new SendMessageRequest();
        request.setMessage("this is a test message");

        final byte[] data = marshaller.marshall(request);

        // WHEN
        final SendMessageRequest unmarshalledRequest = SendMessageRequest.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(request, unmarshalledRequest);
    }

    @Test
    public void testSendMessageResponse() throws MarshallingException {
        // PREPARE
        final SendMessageRequest request = new SendMessageRequest();
        request.setMessage("this is a test message");

        final SendMessageResponse response = new SendMessageResponse(request);

        final byte[] data = marshaller.marshall(response);

        // WHEN
        final SendMessageResponse unmarshalledResponse = SendMessageResponse.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(response, unmarshalledResponse);
    }

    @Test
    public void testSendPrivateMessageRequest() throws MarshallingException {
        // PREPARE
        final SendPrivateMessageRequest request = new SendPrivateMessageRequest();
        request.setMessage("this is a private test message");
        request.setSender("me@localhost.local");

        final byte[] data = marshaller.marshall(request);

        // WHEN
        final SendPrivateMessageRequest unmarshalledRequest = SendPrivateMessageRequest.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(request, unmarshalledRequest);
    }

    @Test
    public void testSendPrivateMessageResponse() throws MarshallingException {
        // PREPARE
        final SendPrivateMessageRequest request = new SendPrivateMessageRequest();
        request.setMessage("this is a private test message");
        request.setSender("me@localhost.local");

        final SendPrivateMessageResponse response = new SendPrivateMessageResponse(request);

        final byte[] data = marshaller.marshall(response);

        // WHEN
        final SendPrivateMessageResponse unmarshalledResponse = SendPrivateMessageResponse.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(response, unmarshalledResponse);
    }

    @Test
    public void testTamperedRequest() throws MarshallingException {
        // PREPARE
        final SendPrivateMessageRequest originalRequest = new SendPrivateMessageRequest();
        originalRequest.setMessage("this is a private test message");
        originalRequest.setSender("me@localhost.local");

        final TamperedRequest request = new TamperedRequest(originalRequest);

        final byte[] data = marshaller.marshall(request);

        // WHEN
        final TamperedRequest unmarshalledRequest = TamperedRequest.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(request, unmarshalledRequest);
    }

    @Test
    public void testTamperedResponse() throws MarshallingException {
        // PREPARE
        final SendPrivateMessageRequest originalRequest = new SendPrivateMessageRequest();
        originalRequest.setMessage("this is a private test message");
        originalRequest.setSender("me@localhost.local");

        final TamperedRequest request = new TamperedRequest(originalRequest);

        final TamperedResponse response = new TamperedResponse(request);
        response.setReason("some test reason");

        final byte[] data = marshaller.marshall(response);

        // WHEN
        final TamperedResponse unmarshalledResponse = TamperedResponse.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(response, unmarshalledResponse);
    }
}
