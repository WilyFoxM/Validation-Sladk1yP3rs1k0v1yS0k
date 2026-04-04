package ru.wilyfox.client.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public final class DwCooldownValueDecoder {
    private DwCooldownValueDecoder() {
    }

    public static DwCooldownValuePacket decode(byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

        try {
            return new DwCooldownValuePacket(DwProtocolCodec.readLong(buf));
        } finally {
            buf.release();
        }
    }
}
