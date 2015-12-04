package marshalling;

import messages.*;

public interface MessageMarshaller {
    byte[] marshall(Message message) throws MarshallingException;
    Message unmarshall(byte[] data) throws MarshallingException;
    
    byte[] marshallAuthConfirmationResponse(AuthConfirmationResponse response) throws MarshallingException;
    AuthConfirmationResponse unmarshallAuthConfirmationResponse(byte[] data) throws MarshallingException;
    
    byte[] marshallAuthConfirmationRequest(AuthConfirmationRequest request) throws MarshallingException;
    AuthConfirmationRequest unmarshallAuthConfirmationRequest(byte[] data) throws MarshallingException;
    
    byte[] marshallAuthenticateResponse(AuthenticateResponse response) throws MarshallingException;
    AuthenticateResponse unmarshallAuthenticateResponse(byte[] data) throws MarshallingException;
    
    byte[] marshallAuthenticateRequest(AuthenticateRequest response) throws MarshallingException;
    AuthenticateRequest unmarshallAuthenticateRequest(byte[] data) throws MarshallingException;

    byte[] marshallErrorResponse(ErrorResponse response) throws MarshallingException;
    ErrorResponse unmarshallErrorResponse(byte[] data) throws MarshallingException;

    byte[] marshallListRequest(ListRequest request) throws MarshallingException;
    ListRequest unmarshallListRequest(byte[] data) throws MarshallingException;

    byte[] marshallListResponse(ListResponse response) throws MarshallingException;
    ListResponse unmarshallListResponse(byte[] data) throws MarshallingException;

    byte[] marshallLoginRequest(LoginRequest request) throws MarshallingException;
    LoginRequest unmarshallLoginRequest(byte[] data) throws MarshallingException;

    byte[] marshallLoginResponse(LoginResponse response) throws MarshallingException;
    LoginResponse unmarshallLoginResponse(byte[] data) throws MarshallingException;

    byte[] marshallLogoutRequest(LogoutRequest request) throws MarshallingException;
    LogoutRequest unmarshallLogoutRequest(byte[] data) throws MarshallingException;

    byte[] marshallLogoutResponse(LogoutResponse response) throws MarshallingException;
    LogoutResponse unmarshallLogoutResponse(byte[] data) throws MarshallingException;

    byte[] marshallLookupRequest(LookupRequest request) throws MarshallingException;
    LookupRequest unmarshallLookupRequest(byte[] data) throws MarshallingException;

    byte[] marshallLookupResponse(LookupResponse response) throws MarshallingException;
    LookupResponse unmarshallLookupResponse(byte[] data) throws MarshallingException;

    byte[] marshallRegisterRequest(RegisterRequest request) throws MarshallingException;
    RegisterRequest unmarshallRegisterRequest(byte[] data) throws MarshallingException;

    byte[] marshallRegisterResponse(RegisterResponse response) throws MarshallingException;
    RegisterResponse unmarshallRegisterResponse(byte[] data) throws MarshallingException;

    byte[] marshallSendMessageRequest(SendMessageRequest request) throws MarshallingException;
    SendMessageRequest unmarshallSendMessageRequest(byte[] data) throws MarshallingException;

    byte[] marshallSendMessageResponse(SendMessageResponse response) throws MarshallingException;
    SendMessageResponse unmarshallSendMessageResponse(byte[] data) throws MarshallingException;

    byte[] marshallSendPrivateMessageRequest(SendPrivateMessageRequest request) throws MarshallingException;
    SendPrivateMessageRequest unmarshallSendPrivateMessageRequest(byte[] data) throws MarshallingException;

    byte[] marshallSendPrivateMessageResponse(SendPrivateMessageResponse response) throws MarshallingException;
    SendPrivateMessageResponse unmarshallSendPrivateMessageResponse(byte[] data) throws MarshallingException;

    byte[] marshallUnknownRequest(UnknownRequest request) throws MarshallingException;
    UnknownRequest unmarshallUnknownRequest(byte[] data) throws MarshallingException;

    byte[] marshallTamperedRequest(TamperedRequest request) throws MarshallingException;
    TamperedRequest unmarshallTamperedRequest(byte[] data) throws MarshallingException;

    byte[] marshallTamperedResponse(TamperedResponse response) throws MarshallingException;
    TamperedResponse unmarshallTamperedResponse(byte[] data) throws MarshallingException;

    byte[] marshallExitEvent(ExitEvent event) throws MarshallingException;
    ExitEvent unmarshallExitEvent(byte[] data) throws MarshallingException;

    byte[] marshallMessageEvent(MessageEvent event) throws MarshallingException;
    MessageEvent unmarshallMessageEvent(byte[] data) throws MarshallingException;

    byte[] marshallUserPresenceChangedEvent(UserPresenceChangedEvent event) throws MarshallingException;
    UserPresenceChangedEvent unmarshallUserPresenceChangedEvent(byte[] data) throws MarshallingException;
}
