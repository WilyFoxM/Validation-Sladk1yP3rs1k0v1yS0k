package ru.wilyfox.client.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public final class DwGameEventDecoder {
    private DwGameEventDecoder() {
    }

    public static DwGameEventPacket decode(byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

        try {
            int ordinal = DwProtocolCodec.readVarInt(buf);
            DwGameEvent[] values = DwGameEvent.values();
            if (ordinal < 0 || ordinal >= values.length) {
                throw new IllegalArgumentException("Unknown DW gameevent ordinal: " + ordinal);
            }
            return new DwGameEventPacket(values[ordinal]);
        } finally {
            buf.release();
        }
    }
}
