package com.wataru.blockchain.core.net;

import com.wataru.blockchain.core.net.packet.BlockPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NodeCoreEncoder extends MessageToByteEncoder<BlockPacket> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, BlockPacket blockPacket, ByteBuf byteBuf) throws Exception {
        byte[] bytes = blockPacket.toBytes();
        int length = bytes.length;
        ByteBuf buf = Unpooled.buffer(4 + length);
        buf.writeInt(length);
        buf.writeBytes(bytes);
        byteBuf.writeBytes(buf);
    }
}
