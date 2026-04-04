package ru.wilyfox.client.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class DwBossTimersDecoder {
    private static final Pattern BOSS_ID_PATTERN = Pattern.compile("[A-Za-z0-9_:-]{1,64}");
    private static final long MIN_REMAINING_MILLIS = -604_800_000L;
    private static final long MAX_REMAINING_MILLIS = 31_536_000_000L;

    private DwBossTimersDecoder() {
    }

    public static DwBossTimersPacket decode(byte[] data) {
        return decode(data, Set.of());
    }

    public static DwBossTimersPacket decode(byte[] data, Set<String> knownBossIds) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

        try {
            int count = DwProtocolCodec.readVarInt(buf);
            if (count < 0 || count > 1024) {
                throw new IllegalArgumentException("DW bosstimers count is out of range: " + count);
            }

            Map<String, Long> timers = new LinkedHashMap<>(Math.max(4, count));
            for (int i = 0; i < count; i++) {
                String bossId = DwProtocolCodec.readString(buf);
                long remainingMillis = DwProtocolCodec.readLong(buf);

                validateEntry(bossId, remainingMillis, knownBossIds);
                timers.put(bossId, remainingMillis);
            }

            return new DwBossTimersPacket(timers);
        } finally {
            buf.release();
        }
    }

    private static void validateEntry(String bossId, long remainingMillis, Set<String> knownBossIds) {
        if (bossId == null || !BOSS_ID_PATTERN.matcher(bossId).matches()) {
            throw new IllegalArgumentException("DW bosstimers bossId is invalid: " + bossId);
        }

        if (remainingMillis < MIN_REMAINING_MILLIS || remainingMillis > MAX_REMAINING_MILLIS) {
            throw new IllegalArgumentException("DW bosstimers remainingMillis is out of range: " + remainingMillis);
        }

        if (knownBossIds != null && !knownBossIds.isEmpty() && !knownBossIds.contains(bossId)) {
            throw new IllegalArgumentException("DW bosstimers bossId is unknown: " + bossId);
        }
    }
}
