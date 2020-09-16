package com.wataru.blockchain.core.net.packet.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wataru.blockchain.core.primitive.block.Block;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties
public class AddBlockPayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private Block block;

    public AddBlockPayload() {}

    public AddBlockPayload(Block block) {
        this.block = block;
    }
}
