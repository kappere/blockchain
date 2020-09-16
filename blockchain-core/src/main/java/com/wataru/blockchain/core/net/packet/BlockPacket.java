package com.wataru.blockchain.core.net.packet;

import com.wataru.blockchain.core.util.JsonUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class BlockPacket<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private T data;
    private String uuid;
    private Boolean spread;

    public BlockPacket() {}

    public BlockPacket(T data, Boolean spread) {
        this.data = data;
        this.uuid = UUID.randomUUID().toString();
        this.spread = spread;
    }

    public BlockPacket(T data, String uuid) {
        this.data = data;
        this.uuid = uuid;
    }

    public byte[] toBytes() {
        return JsonUtil.serialize(this);
    }
}
