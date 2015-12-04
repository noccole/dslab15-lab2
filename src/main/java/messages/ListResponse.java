package messages;

import entities.User;
import marshalling.MarshallingException;
import marshalling.MessageMarshaller;

import java.util.Map;

public class ListResponse extends Response {
    private Map<String, User.Presence> userList;

    public ListResponse(ListRequest request) {
        super(request);
    }

    public Map<String, User.Presence> getUserList() {
        return userList;
    }

    public void setUserList(Map<String, User.Presence> userList) {
        this.userList = userList;
    }

    @Override
    public String toString() {
        return "list reply";
    }

    @Override
    public byte[] marshall(MessageMarshaller marshaller) throws MarshallingException {
        return marshaller.marshallListResponse(this);
    }
}
