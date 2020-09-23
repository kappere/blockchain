package com.wataru.blockchain.core.net.packet.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties
@EqualsAndHashCode(callSuper = true)
public class HeartbeatPayload extends BasePayload {
    private static final long serialVersionUID = 1L;
}
