package ru.wilyfox.utils;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class WorldToScreen {
    private WorldToScreen() {
    }

    public static ScreenPoint project(Vec3 worldPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null) {
            return null;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        Vec3 relative = worldPos.subtract(cameraPos);

        Vector3f cameraSpace = new Vector3f(
                (float) relative.x,
                (float) relative.y,
                (float) relative.z
        );

        Quaternionf rotation = new Quaternionf(camera.rotation());
        rotation.conjugate();
        cameraSpace.rotate(rotation);

        // В Minecraft перед камерой обычно -Z
        float depth = -cameraSpace.z;
        if (depth <= 0.01f) {
            return null;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float aspect = (float) screenWidth / (float) screenHeight;
        float fov = (float) mc.options.fov().get();
        float tanHalfFov = (float) Math.tan(Math.toRadians(fov / 2.0f));

        float ndcX = cameraSpace.x / (depth * tanHalfFov * aspect);
        float ndcY = cameraSpace.y / (depth * tanHalfFov);

        if (ndcX < -1.2f || ndcX > 1.2f || ndcY < -1.2f || ndcY > 1.2f) {
            return null;
        }

        int screenX = Math.round((ndcX * 0.5f + 0.5f) * screenWidth);
        int screenY = Math.round((1.0f - (ndcY * 0.5f + 0.5f)) * screenHeight);

        return new ScreenPoint(screenX, screenY);
    }

    public record ScreenPoint(int x, int y) {
    }
}
