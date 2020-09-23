package com.wataru.blockchain.core.net.packet.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wataru.blockchain.core.primitive.block.Block;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@JsonIgnoreProperties
@EqualsAndHashCode(callSuper = true)
public class AddBlockPayload extends BasePayload {
    private static final long serialVersionUID = 1L;
    protected byte[] data;

    public AddBlockPayload() {}

    public AddBlockPayload(Block block) {
        this.data = block.serialize();
    }

    public Block toBlock() {
        Block b = new Block();
        b.deserialize(data);
        return b;
    }
}
