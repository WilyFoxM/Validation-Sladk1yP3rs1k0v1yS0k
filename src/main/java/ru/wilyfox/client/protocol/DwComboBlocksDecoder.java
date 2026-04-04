package ru.wilyfox.client.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public final class DwComboBlocksDecoder {
    private DwComboBlocksDecoder() {
    }

    public static DwComboBlocksPacket decode(byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

        try {
            return new DwComboBlocksPacket(DwProtocolCodec.readInt(buf));
        } finally {
            buf.release();
        }
    }
}
