package entities;

import java.util.ArrayList;
import java.util.Collection;

public class User {
    public enum Presence {
        Available,
        Offline
    }

    private final String username;
    private String password;
    private Presence presence;
    private Collection<PrivateAddress> privateAddresses = new ArrayList<>();

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

    public Collection<PrivateAddress> getPrivateAddresses() {
        return privateAddresses;
    }

    public void addPrivateAddress(PrivateAddress address) {
        privateAddresses.add(address);
    }

    public void clearPrivateAddresses() {
        privateAddresses.clear();
    }
}
