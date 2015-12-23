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

    public ListResponse(long messageId) {
        super(messageId);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListResponse)) return false;
        if (!super.equals(o)) return false;

        ListResponse that = (ListResponse) o;

        return !(userList != null ? !userList.equals(that.userList) : that.userList != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (userList != null ? userList.hashCode() : 0);
        return result;
    }
}
