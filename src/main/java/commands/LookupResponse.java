package commands;

public class LookupResponse extends Response {
    private String username;
    private String privateAddress;

    public LookupResponse(LookupRequest request) {
        super(request);

        this.username = request.getUsername();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrivateAddress() {
        return privateAddress;
    }

    public void setPrivateAddress(String privateAddress) {
        this.privateAddress = privateAddress;
    }

    @Override
    public String toString() {
        return "lookup result";
    }
}
