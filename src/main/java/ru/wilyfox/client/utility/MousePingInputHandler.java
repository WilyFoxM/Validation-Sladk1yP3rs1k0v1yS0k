package ru.wilyfox.client.utility;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;
import ru.wilyfox.client.chat.FrogChatProtocol;
import ru.wilyfox.client.ping.PingMarkerManager;
import ru.wilyfox.client.ping.PingPayload;

public final class MousePingInputHandler {
    private boolean middleMouseWasPressed = false;

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                middleMouseWasPressed = false;
                return;
            }

            long window = client.getWindow().getWindow();
            boolean middleMousePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

            if (middleMousePressed && !middleMouseWasPressed && client.screen == null) {
                PingPayload payload = PingPayload.captureCurrent();
                if (payload != null) {
                    PingMarkerManager.addLocalMarker(payload);
                    FrogChatProtocol.sendPingPayload(payload.toJson());
                }
            }

            middleMouseWasPressed = middleMousePressed;
        });
    }
}
