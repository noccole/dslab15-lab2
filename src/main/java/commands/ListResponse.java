package commands;

import java.util.Map;

public class ListResponse extends Response {
    private Map<String, String> users;

    public ListResponse(ListRequest request) {
        super(request);
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "list reply";
    }
}
