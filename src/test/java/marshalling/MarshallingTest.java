package marshalling;

import messages.LoginRequest;
import messages.LoginResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class MarshallingTest {
    private final MessageMarshaller marshaller;

    public MarshallingTest(MessageMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Test
    public void testLoginRequest() throws MarshallingException {
        // PREPARE
        final LoginRequest request = new LoginRequest();
        request.setUsername("username");
        request.setPassword("12345");

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
        request.setUsername("username");
        request.setPassword("12345");

        final LoginResponse response = new LoginResponse(request);
        response.setResponse(LoginResponse.ResponseCode.UserAlreadyLoggedIn);

        final byte[] data = marshaller.marshall(response);

        // WHEN
        final LoginResponse unmarshalledResponse = LoginResponse.class.cast(marshaller.unmarshall(data));

        // THEN
        assertEquals(response, unmarshalledResponse);
    }
}
