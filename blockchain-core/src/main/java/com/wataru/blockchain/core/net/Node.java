package com.wataru.blockchain.core.net;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ip;
    private int port;
    private long expireTime;

    public Node() {
    }

    public static Node parseString(String addr) {
        String[] s = addr.split(":");
        return new Node(s[0], Integer.parseInt(s[1]));
    }

    public Node(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean validate() {
        return ip != null && port > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Node node = (Node) o;
        return port == node.port && Objects.equals(ip, node.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public String toString() {
        return ip + ":" + port;
    }
}