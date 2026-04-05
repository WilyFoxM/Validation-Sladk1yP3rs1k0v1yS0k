package ru.wilyfox.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.wilyfox.client.performance.EstimatedTpsMonitor;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void froghelper$trackEstimatedTps(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        EstimatedTpsMonitor.onClientboundPacket(packet);
    }
}
