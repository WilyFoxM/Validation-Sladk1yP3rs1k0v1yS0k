package ru.wilyfox.client.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import static ru.wilyfox.FrogHelper.LOGGER;

final class ProtocolRouter {
    void route(ProtocolState state, byte[] data) {
        ProtocolEnvelope envelope = tryReadEnvelope(data);
        int length = data.length;

        if (envelope != null) {
            LOGGER.info("DW protocol: received dw:evoplus payload, bytes={}, typeId={}", length, envelope.typeId());
            ProtocolDebugLogger.logPayloadSampleIfNeeded(state, envelope.typeId(), envelope.body());
        } else {
            LOGGER.info("DW protocol: received dw:evoplus payload, bytes={}, typeId=<unreadable>", length);
            ProtocolDebugLogger.logUnknownPayload("unreadable typeId", data);
            return;
        }

        String typeId = envelope.typeId();
        byte[] body = envelope.body();

        switch (typeId) {
            case "bosstimers" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleBossTimers(state, body));
            case "bosstypes" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleBossTypes(state, body));
            case "activerunes" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleActiveRunes(state, body));
            case "serverinfo" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleServerInfo(state, body));
            case "pettypes" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handlePetTypes(state, body));
            case "potiontypes" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handlePotionTypes(state, body));
            case "sellers" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleSellers(state, body));
            case "combo" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleCombo(state, body));
            case "comboblocks" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleComboBlocks(state, body));
            case "potiontimers" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handlePotionTimers(state, body));
            case "statisticinfo" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleStatisticInfo(state, body));
            case "fishingpots" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleFishingSpots(state, body));
            case "spotnibbles" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleSpotNibbles(state, body));
            case "stafftypes" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleStaffTypes(state, body));
            case "stafftimers" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleStaffTimers(state, body));
            case "abilitytypes" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleAbilityTypes(state, body));
            case "abilitytimers" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleAbilityTimers(state, body));
            case "bossdamage" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleBossDamage(state, body));
            case "levelinfo" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleLevelInfo(state, body));
            case "harpooncd" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleHarpoonCooldown(body));
            case "marketcd" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleNamedCooldown("marketcd", "Market", body));
            case "gourmetcd" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleGourmetCooldown(state, body));
            case "potioncd" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleNamedCooldown("potioncd", "Potion", body));
            case "token" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleToken(body));
            case "boosters" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleBoosters(state, body));
            case "gameevent" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleGameEvent(state, body));
            case "claninfo" -> dispatch(typeId, body, () -> ProtocolPayloadHandlers.handleClanInfo(state, body));
            default -> ProtocolDebugLogger.logUnknownPayload("unknown typeId=" + typeId, body);
        }
    }

    private void dispatch(String subchannel, byte[] data, PayloadHandler handler) {
        try {
            if (!handler.handle()) {
                ProtocolDebugLogger.logDecodeFailure(subchannel, data, null);
            }
        } catch (Exception exception) {
            ProtocolDebugLogger.logDecodeFailure(subchannel, data, exception);
            throw exception;
        }
    }

    private ProtocolEnvelope tryReadEnvelope(byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

        try {
            String typeId = DwProtocolCodec.readString(buf);
            byte[] body = new byte[buf.readableBytes()];
            buf.readBytes(body);
            return new ProtocolEnvelope(typeId, body);
        } catch (Exception ignored) {
            return null;
        } finally {
            buf.release();
        }
    }

    private record ProtocolEnvelope(String typeId, byte[] body) {
    }

    @FunctionalInterface
    private interface PayloadHandler {
        boolean handle();
    }
}
