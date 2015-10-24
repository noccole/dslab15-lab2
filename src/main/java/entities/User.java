package entities;

public class User {
    public enum Presence {
        Available,
        Offline
    }

    private final String username;
    private String password;
    private Presence presence;
    private PrivateAddress privateAddress;

    public User(String username) {
        this.username = username;
        this.presence = Presence.Offline;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Presence getPresence() {
        return presence;
    }

    public void setPresence(Presence presence) {
        this.presence = presence;
    }

    public PrivateAddress getPrivateAddress() {
        return privateAddress;
    }

    public void setPrivateAddress(PrivateAddress privateAddress) {
        this.privateAddress = privateAddress;
    }
}
