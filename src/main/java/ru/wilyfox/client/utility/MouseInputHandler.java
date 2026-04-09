package ru.wilyfox.client.utility;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;
import ru.wilyfox.client.hud.HudRenderer;
import ru.wilyfox.client.profiler.ModProfiler;

import static ru.wilyfox.utils.MouseUtils.getMouseX;
import static ru.wilyfox.utils.MouseUtils.getMouseY;

public final class MouseInputHandler {
    private final HudRenderer hudRenderer;

    private boolean leftMouseWasPressed = false;

    public MouseInputHandler(HudRenderer h) {
        this.hudRenderer = h;
    }

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try (ModProfiler.Scope ignored = ModProfiler.getInstance().scope("tick/MouseInputHandler")) {
                if (client.player == null) {
                    return;
                }

                if (!hudRenderer.isEditing() && !hudRenderer.isSettingsOpen()) {
                    leftMouseWasPressed = false;
                    return;
                }

                long window = client.getWindow().getWindow();
                boolean leftMousePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

                double mouseX = getMouseX();
                double mouseY = getMouseY();

                int screenWidth = client.getWindow().getGuiScaledWidth();
                int screenHeight = client.getWindow().getGuiScaledHeight();

                if (leftMousePressed && !leftMouseWasPressed) {
                    hudRenderer.onMousePressed(mouseX, mouseY, 0);
                }

                if (leftMousePressed) {
                    hudRenderer.onMouseDragged(mouseX, mouseY, screenWidth, screenHeight, 0);
                }

                if (!leftMousePressed && leftMouseWasPressed) {
                    hudRenderer.onMouseReleased(0, screenWidth, screenHeight, mouseX, mouseY);
                }

                leftMouseWasPressed = leftMousePressed;
            }
        });
    }
}
