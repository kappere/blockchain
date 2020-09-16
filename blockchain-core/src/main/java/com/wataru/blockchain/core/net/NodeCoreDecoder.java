package com.wataru.blockchain.core.net;

import com.wataru.blockchain.core.net.packet.BlockPacket;
import com.wataru.blockchain.core.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;

public class NodeCoreDecoder extends LengthFieldBasedFrameDecoder {

    public NodeCoreDecoder() {
        super(Integer.MAX_VALUE, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf w = (ByteBuf) super.decode(ctx, in);
        if (w == null) {
            return null;
        }
        byte[] bytes = new byte[w.capacity()];
        w.getBytes(0, bytes);
//        ReferenceCountUtil.release(w);
        ReferenceCountUtil.release(in);
        return JsonUtil.deserialize(bytes, BlockPacket.class);
    }
}
