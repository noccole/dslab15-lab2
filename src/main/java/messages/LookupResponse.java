package messages;

import entities.PrivateAddress;

public class LookupResponse extends Response {
    private PrivateAddress privateAddress;

    public LookupResponse(LookupRequest request) {
        super(request);
    }

    public PrivateAddress getPrivateAddress() {
        return privateAddress;
    }

    public void setPrivateAddress(PrivateAddress privateAddress) {
        this.privateAddress = privateAddress;
    }

    @Override
    public String toString() {
        return "lookup result";
    }
}
