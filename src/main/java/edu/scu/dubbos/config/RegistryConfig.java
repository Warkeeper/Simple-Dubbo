package edu.scu.dubbos.config;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 10:40
 */
public class RegistryConfig {
    private String address;

    public RegistryConfig() {
    }

    public RegistryConfig(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHost() {
        int i = address.lastIndexOf(':');
        String host;
        int port;
        if (i >= 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        return host;
    }

    public int getPort() {
        int i = address.lastIndexOf(':');
        int port = 2181;
        if (i >= 0) {
            port = Integer.parseInt(address.substring(i + 1));
        }
        return port;
    }

    public void checkAndUpdate() {
        if (address == null) {
            throw new IllegalStateException("Registry's address is not allowed to be null");
        }
    }
}
