package ru.wilyfox.client.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public final class DwLevelInfoDecoder {
    private DwLevelInfoDecoder() {
    }

    public static DwLevelInfoPacket decode(byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

        try {
            int level = DwProtocolCodec.readVarInt(buf);
            double money = DwProtocolCodec.readDouble(buf);
            int blocks = DwProtocolCodec.readInt(buf);
            boolean maxLevel = DwProtocolCodec.readBoolean(buf);
            double requiredMoney = 0.0D;
            int requiredBlocks = 0;

            if (!maxLevel) {
                requiredMoney = DwProtocolCodec.readDouble(buf);
                requiredBlocks = DwProtocolCodec.readInt(buf);
            }

            return new DwLevelInfoPacket(level, money, blocks, requiredMoney, requiredBlocks, maxLevel);
        } finally {
            buf.release();
        }
    }
}
