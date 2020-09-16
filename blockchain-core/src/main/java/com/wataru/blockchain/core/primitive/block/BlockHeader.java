package com.wataru.blockchain.core.primitive.block;

import com.wataru.blockchain.core.util.EncodeUtil;
import com.wataru.blockchain.core.util.JsonUtil;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BlockHeader {
    /**
     * 版本
     */
    private int version;

    /**
     * 前一个区块的Hash值
     */
    private String hashPrevBlock;

    /**
     * 包含交易信息的Merkle树根
     */
    private String hashMerkleRoot;

    /**
     * 区块产生的时间
     */
    private long time;

    /**
     * 工作量证明(POW)的难度
     */
    private int bits;

    /**
     * 要找的符合POW的随机数
     */
    private int nonce;

    public String getHash() {
        return EncodeUtil.bytesToHexString(EncodeUtil.sha256(JsonUtil.serialize(this)));
    }
}
