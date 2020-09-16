package com.wataru.blockchain.core.net.packet.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties
public class ResponsePayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private Object data;

    public ResponsePayload() {}

    public ResponsePayload(Object data) {
        this.data = data;
    }
}
