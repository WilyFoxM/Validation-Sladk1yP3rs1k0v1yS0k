package ru.wilyfox.mixin;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import ru.wilyfox.bridge.ChatComponentAccessor;

import java.util.List;

@Mixin(ChatComponent.class)
public interface ChatComponentAccessorMixin extends ChatComponentAccessor {
    @Override
    @Accessor("trimmedMessages")
    List<GuiMessage.Line> froghelper$getTrimmedMessages();

    @Override
    @Invoker("screenToChatX")
    double froghelper$screenToChatX(double mouseX);

    @Override
    @Invoker("screenToChatY")
    double froghelper$screenToChatY(double mouseY);

    @Override
    @Invoker("getMessageLineIndexAt")
    int froghelper$getMessageLineIndexAt(double chatX, double chatY);
}
