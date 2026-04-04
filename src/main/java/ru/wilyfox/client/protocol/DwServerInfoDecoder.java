package ru.wilyfox.client.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public final class DwServerInfoDecoder {
    private DwServerInfoDecoder() {
    }

    public static CurrentServerInfo decode(byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

        try {
            String serverName = DwProtocolCodec.readString(buf);
            int mirror = DwProtocolCodec.readInt(buf);
            return CurrentServerInfo.fromProtocol(serverName, mirror);
        } finally {
            buf.release();
        }
    }
}
