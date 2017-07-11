package com.fjsh.rpc.common;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 继承ByteToMessageDecoder 自己处理半包问题
 * 或者基于其他策略进行处理
 * @author Administrator
 *
 */

public class RpcDecoder extends ByteToMessageDecoder{

	private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        System.out.println("data.length"+dataLength);
        if (dataLength < 0) {
            ctx.close();
        }
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return ;
        }
        byte[] data = new byte[dataLength];
        try {
			in.readBytes(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        Object obj = SerializationUtil.deserialize(data, genericClass);
        out.add(obj);
    }
}
