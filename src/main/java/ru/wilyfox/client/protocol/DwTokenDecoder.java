package ru.wilyfox.client.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public final class DwTokenDecoder {
    private DwTokenDecoder() {
    }

    public static DwTokenPacket decode(byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

        try {
            String value = DwProtocolCodec.readBoolean(buf) ? DwProtocolCodec.readString(buf) : null;
            return new DwTokenPacket(value);
        } finally {
            buf.release();
        }
    }
}
