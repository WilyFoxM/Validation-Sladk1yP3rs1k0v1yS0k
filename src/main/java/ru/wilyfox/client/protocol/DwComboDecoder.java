package ru.wilyfox.client.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public final class DwComboDecoder {
    private DwComboDecoder() {
    }

    public static DwComboPacket decode(byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

        try {
            double booster = DwProtocolCodec.readDouble(buf);
            double nextBooster = DwProtocolCodec.readDouble(buf);
            int blocks = DwProtocolCodec.readInt(buf);
            int requiredBlocks = DwProtocolCodec.readInt(buf);
            return new DwComboPacket(booster, nextBooster, blocks, requiredBlocks);
        } finally {
            buf.release();
        }
    }
}
