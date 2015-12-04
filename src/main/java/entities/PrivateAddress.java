package entities;

import java.io.Serializable;

public class PrivateAddress implements Serializable {
    private String hostname;
    private Integer port;

    public PrivateAddress() {
        this.hostname = "";
        this.port = 0;
    }

    public PrivateAddress(String address) {
        hostname = address.substring(0, address.indexOf(":"));
        port = Integer.valueOf(address.substring(address.indexOf(":") + 1));
    }

    public PrivateAddress(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return hostname + ":" + port;
    }
}
