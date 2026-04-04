package ru.wilyfox.client.highlight;

import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.PlayerWallHeadBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.wilyfox.client.hud.config.ConfigManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UsefulWorldHighlightRenderHook {
    private static final int GOLDEN_CRYSTAL_MODEL_ID = 271;
    private static final int ENTITY_SCAN_INTERVAL_TICKS = 10;
    private static final int BLOCK_SCAN_REFRESH_TICKS = 100;
    private static final int DIRTY_CHUNK_RESCAN_LIMIT = 2;
    private static final int HORIZONTAL_SCAN_RADIUS = 32;
    private static final int VERTICAL_SCAN_RADIUS = 20;
    private static final float LINE_ALPHA = 1.0F;
    private static final double GOLDEN_CRYSTAL_CLUSTER_RADIUS = 1.6D;
    private static final String GOLDEN_CRYSTAL_MODEL_PREFIX = "modelengine:fragment_";
    private static final String GOLDEN_CRYSTAL_FIRE_PREFIX = "modelengine:internal_fire/";
    private static final double GOLDEN_CRYSTAL_BOX_WIDTH = 0.58D;
    private static final double GOLDEN_CRYSTAL_BOX_HEIGHT = 0.46D;
    private static final double GOLDEN_CRYSTAL_BOX_Y_OFFSET = 0.28D;
    private static final RenderType USEFUL_HIGHLIGHT_NO_DEPTH = RenderType.create(
            "froghelper_useful_highlight_no_depth",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING_FORWARD)
                    .setLineState(RenderStateShard.DEFAULT_LINE)
                    .createCompositeState(false)
    );

    private static final List<ColoredBox> CACHED_BOXES = new ArrayList<>();
    private static final List<ColoredBox> BLOCK_BOXES = new ArrayList<>();
    private static final List<ColoredBox> ENTITY_BOXES = new ArrayList<>();
    private static final Map<Long, ChunkScanResult> BLOCK_CHUNK_CACHE = new HashMap<>();
    private static final Set<Long> DIRTY_CHUNK_KEYS = new HashSet<>();
    private static long lastEntityScanTick = Long.MIN_VALUE;
    private static long lastBlockRefreshTick = Long.MIN_VALUE;
    private static int lastMinChunkX = Integer.MIN_VALUE;
    private static int lastMaxChunkX = Integer.MIN_VALUE;
    private static int lastMinChunkZ = Integer.MIN_VALUE;
    private static int lastMaxChunkZ = Integer.MIN_VALUE;

    private UsefulWorldHighlightRenderHook() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(UsefulWorldHighlightRenderHook::onAfterEntities);
    }

    public static void markBlockDirty(BlockPos blockPos) {
        if (blockPos == null) {
            return;
        }

        DIRTY_CHUNK_KEYS.add(ChunkPos.asLong(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ())));
    }

    private static void onAfterEntities(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || context.matrixStack() == null || context.consumers() == null) {
            clearCache();
            return;
        }

        if (!ConfigManager.get().render.usefulItemsHighlight) {
            clearCache();
            return;
        }

        refreshCacheIfNeeded(mc);
        if (CACHED_BOXES.isEmpty()) {
            return;
        }

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = context.matrixStack();
        VertexConsumer lineConsumer = context.consumers().getBuffer(USEFUL_HIGHLIGHT_NO_DEPTH);

        for (ColoredBox coloredBox : CACHED_BOXES) {
            ShapeRenderer.renderLineBox(
                    poseStack,
                    lineConsumer,
                    coloredBox.box.move(-cameraPos.x, -cameraPos.y, -cameraPos.z),
                    coloredBox.red,
                    coloredBox.green,
                    coloredBox.blue,
                    LINE_ALPHA
            );
        }
    }

    private static void refreshCacheIfNeeded(Minecraft mc) {
        refreshBlockCacheIfNeeded(mc);
        refreshEntityCacheIfNeeded(mc);

        CACHED_BOXES.clear();
        CACHED_BOXES.addAll(BLOCK_BOXES);
        CACHED_BOXES.addAll(ENTITY_BOXES);
    }

    private static void refreshBlockCacheIfNeeded(Minecraft mc) {
        long gameTime = mc.level.getGameTime();
        BlockPos playerPos = mc.player.blockPosition();
        int minChunkX = SectionPos.blockToSectionCoord(playerPos.getX() - HORIZONTAL_SCAN_RADIUS);
        int maxChunkX = SectionPos.blockToSectionCoord(playerPos.getX() + HORIZONTAL_SCAN_RADIUS);
        int minChunkZ = SectionPos.blockToSectionCoord(playerPos.getZ() - HORIZONTAL_SCAN_RADIUS);
        int maxChunkZ = SectionPos.blockToSectionCoord(playerPos.getZ() + HORIZONTAL_SCAN_RADIUS);

        boolean chunkWindowChanged = minChunkX != lastMinChunkX
                || maxChunkX != lastMaxChunkX
                || minChunkZ != lastMinChunkZ
                || maxChunkZ != lastMaxChunkZ;
        boolean refreshExpired = gameTime - lastBlockRefreshTick >= BLOCK_SCAN_REFRESH_TICKS;
        if (!chunkWindowChanged && !refreshExpired && !BLOCK_BOXES.isEmpty()) {
            processDirtyChunks(mc, minChunkX, maxChunkX, minChunkZ, maxChunkZ);
            return;
        }

        lastBlockRefreshTick = gameTime;
        lastMinChunkX = minChunkX;
        lastMaxChunkX = maxChunkX;
        lastMinChunkZ = minChunkZ;
        lastMaxChunkZ = maxChunkZ;

        Set<Long> activeChunkKeys = new HashSet<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
                activeChunkKeys.add(chunkKey);
                LevelChunk chunk = mc.level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    BLOCK_CHUNK_CACHE.remove(chunkKey);
                    continue;
                }

                ChunkScanResult cached = BLOCK_CHUNK_CACHE.get(chunkKey);
                if (cached == null || refreshExpired || chunkWindowChanged) {
                    BLOCK_CHUNK_CACHE.put(chunkKey, scanChunk(mc, chunk));
                }
            }
        }

        BLOCK_CHUNK_CACHE.keySet().removeIf(chunkKey -> !activeChunkKeys.contains(chunkKey));

        BLOCK_BOXES.clear();
        for (ChunkScanResult result : BLOCK_CHUNK_CACHE.values()) {
            BLOCK_BOXES.addAll(result.boxes);
        }

        processDirtyChunks(mc, minChunkX, maxChunkX, minChunkZ, maxChunkZ);
    }

    private static ChunkScanResult scanChunk(Minecraft mc, LevelChunk chunk) {
        List<ColoredBox> boxes = new ArrayList<>();
        BlockPos playerPos = mc.player.blockPosition();
        int worldMinY = mc.level.dimensionType().minY();
        int worldMaxY = worldMinY + mc.level.dimensionType().height() - 1;
        int minY = Math.max(worldMinY, playerPos.getY() - VERTICAL_SCAN_RADIUS);
        int maxY = Math.min(worldMaxY, playerPos.getY() + VERTICAL_SCAN_RADIUS);
        ChunkPos chunkPos = chunk.getPos();
        int minX = Math.max(chunkPos.getMinBlockX(), playerPos.getX() - HORIZONTAL_SCAN_RADIUS);
        int maxX = Math.min(chunkPos.getMaxBlockX(), playerPos.getX() + HORIZONTAL_SCAN_RADIUS);
        int minZ = Math.max(chunkPos.getMinBlockZ(), playerPos.getZ() - HORIZONTAL_SCAN_RADIUS);
        int maxZ = Math.min(chunkPos.getMaxBlockZ(), playerPos.getZ() + HORIZONTAL_SCAN_RADIUS);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    BlockState blockState = chunk.getBlockState(cursor);
                    HighlightBlockType blockType = HighlightBlockType.from(blockState, chunk.getBlockEntity(cursor));
                    if (blockType == null) {
                        continue;
                    }

                    boxes.add(new ColoredBox(blockType.createBox(cursor, blockState), blockType.red, blockType.green, blockType.blue));
                }
            }
        }

        return new ChunkScanResult(boxes);
    }

    private static void processDirtyChunks(Minecraft mc, int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {
        if (DIRTY_CHUNK_KEYS.isEmpty()) {
            return;
        }

        List<Long> rescannedChunkKeys = new ArrayList<>();
        int rescanned = 0;
        for (long chunkKey : new ArrayList<>(DIRTY_CHUNK_KEYS)) {
            int chunkX = ChunkPos.getX(chunkKey);
            int chunkZ = ChunkPos.getZ(chunkKey);
            if (chunkX < minChunkX || chunkX > maxChunkX || chunkZ < minChunkZ || chunkZ > maxChunkZ) {
                rescannedChunkKeys.add(chunkKey);
                continue;
            }

            LevelChunk chunk = mc.level.getChunkSource().getChunkNow(chunkX, chunkZ);
            if (chunk == null) {
                rescannedChunkKeys.add(chunkKey);
                continue;
            }

            BLOCK_CHUNK_CACHE.put(chunkKey, scanChunk(mc, chunk));
            rescannedChunkKeys.add(chunkKey);
            rescanned++;
            if (rescanned >= DIRTY_CHUNK_RESCAN_LIMIT) {
                break;
            }
        }

        if (rescannedChunkKeys.isEmpty()) {
            return;
        }

        DIRTY_CHUNK_KEYS.removeAll(rescannedChunkKeys);
        BLOCK_BOXES.clear();
        for (ChunkScanResult result : BLOCK_CHUNK_CACHE.values()) {
            BLOCK_BOXES.addAll(result.boxes);
        }
    }

    private static void refreshEntityCacheIfNeeded(Minecraft mc) {
        long gameTime = mc.level.getGameTime();
        if (gameTime == lastEntityScanTick) {
            return;
        }

        if (gameTime - lastEntityScanTick < ENTITY_SCAN_INTERVAL_TICKS && !ENTITY_BOXES.isEmpty()) {
            return;
        }

        lastEntityScanTick = gameTime;
        ENTITY_BOXES.clear();
        collectNearbyEntities(mc);
    }

    private static void collectNearbyEntities(Minecraft mc) {
        AABB searchBox = mc.player.getBoundingBox().inflate(HORIZONTAL_SCAN_RADIUS, VERTICAL_SCAN_RADIUS, HORIZONTAL_SCAN_RADIUS);
        for (Entity entity : mc.level.getEntities(mc.player, searchBox, candidate -> candidate != mc.player)) {
            if (entity.isRemoved()) {
                continue;
            }

            HighlightEntityType entityType = HighlightEntityType.from(entity);
            if (entityType == null) {
                continue;
            }

            ENTITY_BOXES.add(new ColoredBox(entityType.createBox(entity), entityType.red, entityType.green, entityType.blue));
        }
    }

    private static void clearCache() {
        CACHED_BOXES.clear();
        BLOCK_BOXES.clear();
        ENTITY_BOXES.clear();
        BLOCK_CHUNK_CACHE.clear();
        DIRTY_CHUNK_KEYS.clear();
        lastEntityScanTick = Long.MIN_VALUE;
        lastBlockRefreshTick = Long.MIN_VALUE;
        lastMinChunkX = Integer.MIN_VALUE;
        lastMaxChunkX = Integer.MIN_VALUE;
        lastMinChunkZ = Integer.MIN_VALUE;
        lastMaxChunkZ = Integer.MIN_VALUE;
    }

    private record ColoredBox(AABB box, float red, float green, float blue) {
    }

    private record ChunkScanResult(List<ColoredBox> boxes) {
    }

    private enum HighlightEntityType {
        GOLDEN_CRYSTAL(1.0F, 0.82F, 0.15F, 0.12D),
        WORM(1.0F, 0.55F, 0.20F, 0.10D);

        private final float red;
        private final float green;
        private final float blue;
        private final double inflate;

        HighlightEntityType(float red, float green, float blue, double inflate) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.inflate = inflate;
        }

        private static HighlightEntityType from(Entity entity) {
            if (entity instanceof ArmorStand stand) {
                if (isLegacyGoldenCrystal(stand)) {
                    return GOLDEN_CRYSTAL;
                }

                String name = entity.getName().getString();
                if (!name.isBlank() && (name.contains("Червь") || name.contains("Р§РµСЂРІСЊ"))) {
                    return WORM;
                }
            }

            if (entity instanceof Interaction interaction && isClusterGoldenCrystal(interaction)) {
                return GOLDEN_CRYSTAL;
            }

            return null;
        }

        private static boolean isLegacyGoldenCrystal(ArmorStand stand) {
            for (ItemStack stack : stand.getArmorSlots()) {
                if (matchesCustomModel(stack, GOLDEN_CRYSTAL_MODEL_ID)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isClusterGoldenCrystal(Interaction interaction) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return false;
            }

            List<Entity> nearbyEntities = mc.level.getEntities(
                    interaction,
                    interaction.getBoundingBox().inflate(GOLDEN_CRYSTAL_CLUSTER_RADIUS),
                    candidate -> candidate != interaction
            );

            boolean hasEmptyArmorStand = false;
            int goldenCrystalPartCount = 0;
            int firePartCount = 0;

            for (Entity nearbyEntity : nearbyEntities) {
                if (nearbyEntity instanceof ArmorStand stand && isEmptyArmorStand(stand)) {
                    hasEmptyArmorStand = true;
                    continue;
                }

                if (nearbyEntity instanceof Display.ItemDisplay itemDisplay) {
                    String itemModel = readItemModel(itemDisplay.getItemStack());
                    if (itemModel.startsWith(GOLDEN_CRYSTAL_MODEL_PREFIX)) {
                        goldenCrystalPartCount++;
                    } else if (itemModel.startsWith(GOLDEN_CRYSTAL_FIRE_PREFIX)) {
                        firePartCount++;
                    }
                }
            }

            return hasEmptyArmorStand && goldenCrystalPartCount >= 1 && firePartCount >= 1;
        }

        private static boolean isEmptyArmorStand(ArmorStand stand) {
            for (ItemStack stack : stand.getArmorSlots()) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        private AABB createBox(Entity entity) {
            if (this == GOLDEN_CRYSTAL) {
                if (entity instanceof Interaction interaction) {
                    AABB clusterBox = buildGoldenCrystalClusterBox(interaction);
                    if (clusterBox != null) {
                        return clusterBox;
                    }
                }

                return AABB.ofSize(
                        new Vec3(entity.getX(), entity.getY() + GOLDEN_CRYSTAL_BOX_Y_OFFSET, entity.getZ()),
                        GOLDEN_CRYSTAL_BOX_WIDTH,
                        GOLDEN_CRYSTAL_BOX_HEIGHT,
                        GOLDEN_CRYSTAL_BOX_WIDTH
                );
            }

            return entity.getBoundingBox().inflate(inflate);
        }

        private AABB buildGoldenCrystalClusterBox(Interaction interaction) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return null;
            }

            AABB clusterBox = interaction.getBoundingBox();
            boolean foundPart = false;
            for (Entity nearbyEntity : mc.level.getEntities(
                    interaction,
                    interaction.getBoundingBox().inflate(GOLDEN_CRYSTAL_CLUSTER_RADIUS),
                    candidate -> candidate != interaction
            )) {
                if (nearbyEntity instanceof ArmorStand stand && isEmptyArmorStand(stand)) {
                    clusterBox = clusterBox.minmax(stand.getBoundingBox());
                    foundPart = true;
                    continue;
                }

                if (nearbyEntity instanceof Display.ItemDisplay itemDisplay) {
                    String itemModel = readItemModel(itemDisplay.getItemStack());
                    if (itemModel.startsWith(GOLDEN_CRYSTAL_MODEL_PREFIX) || itemModel.startsWith(GOLDEN_CRYSTAL_FIRE_PREFIX)) {
                        clusterBox = clusterBox.minmax(itemDisplay.getBoundingBox());
                        foundPart = true;
                    }
                }
            }

            return foundPart ? clusterBox.inflate(0.08D, 0.12D, 0.08D) : null;
        }
    }

    private enum HighlightBlockType {
        GOLDEN_SHARD(1.0F, 0.79F, 0.16F),
        DIAMOND_SHARD(0.20F, 0.85F, 1.0F),
        BASE_LUCKY_BLOCK(1.0F, 0.92F, 0.15F),
        RARE_LUCKY_BLOCK(0.15F, 0.75F, 1.0F),
        LEGENDARY_LUCKY_BLOCK(0.70F, 0.20F, 1.0F),
        NORMAL_BARREL(0.25F, 0.95F, 0.35F),
        NETHER_BARREL(1.0F, 0.22F, 0.22F),
        END_BARREL(0.74F, 0.28F, 1.0F);

        private static final String GOLDEN_SHARD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTRiZjg5M2ZjNmRlZmFkMjE4Zjc4MzZlZmVmYmU2MzZmMWMyY2MxYmI2NTBjODJmY2NkOTlmMmMxZWU2In19fQ==";
        private static final String DIAMOND_SHARD_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTU5ODUyMjU3OTk4MiwKICAicHJvZmlsZUlkIiA6ICJmMDk3N2NmZWZlZmY0ZGM1OGUyMGIzOTVlMjBiYWJkYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJkaWFtb25kZHVkZTMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjE3NjdmYWEzNjZjODA1Nzc5NTJmNWUwMDc4MTU5ZDU5NzdmMzcyMDJmMzhkNDgxN2Q0YTkyNDVhZDQ4YTkwZCIKICAgIH0KICB9Cn0=";
        private static final String BASE_LUCKY_BLOCK_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM4YzBkMmYxZWMyNjc1NGRjYTNjN2NkYWUzMWYxZjE2NDg4M2Q0NTNlNjg4NjQzZGEwNDc1NjhlN2ZhNWNjOSJ9fX0=";
        private static final String RARE_LUCKY_BLOCK_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmUwMDJkOTc3MjNiOGNjOTgwMmQzMGZlOGU0Y2VmMzYxZTU2Y2YyZTQ5YWU5MWYyNzRkYTcyZjQ3ODEzNDExOCJ9fX0=";
        private static final String LEGENDARY_LUCKY_BLOCK_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTA2ZWExMDRjYjliZTcwM2NjZWQxYjFmNTY1Mjg2NzUyZTI3MTc1MmM1YWM4NWU4MTEzYjNlMmRjNDM1MmMyMCJ9fX0=";

        private final float red;
        private final float green;
        private final float blue;

        HighlightBlockType(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        private static HighlightBlockType from(BlockState blockState, BlockEntity blockEntity) {
            if (blockState.getBlock() instanceof PlayerHeadBlock || blockState.getBlock() instanceof PlayerWallHeadBlock) {
                String texture = readSkullTextureValue(blockEntity);
                if (texture == null) {
                    return null;
                }
                if (GOLDEN_SHARD_TEXTURE.equals(texture)) {
                    return GOLDEN_SHARD;
                }
                if (DIAMOND_SHARD_TEXTURE.equals(texture)) {
                    return DIAMOND_SHARD;
                }
                if (BASE_LUCKY_BLOCK_TEXTURE.equals(texture)) {
                    return BASE_LUCKY_BLOCK;
                }
                if (RARE_LUCKY_BLOCK_TEXTURE.equals(texture)) {
                    return RARE_LUCKY_BLOCK;
                }
                if (LEGENDARY_LUCKY_BLOCK_TEXTURE.equals(texture)) {
                    return LEGENDARY_LUCKY_BLOCK;
                }
                return null;
            }

            if (!(blockState.getBlock() instanceof NoteBlock)) {
                return null;
            }

            NoteBlockInstrument instrument = blockState.getValue(NoteBlock.INSTRUMENT);
            int note = blockState.getValue(NoteBlock.NOTE);
            if (instrument != NoteBlockInstrument.FLUTE) {
                return null;
            }

            return switch (note) {
                case 21, 22 -> NORMAL_BARREL;
                case 18, 19 -> NETHER_BARREL;
                case 16, 17 -> END_BARREL;
                default -> null;
            };
        }

        private AABB createBox(BlockPos blockPos, BlockState blockState) {
            if (blockState.getBlock() instanceof PlayerHeadBlock || blockState.getBlock() instanceof PlayerWallHeadBlock) {
                return new AABB(
                        blockPos.getX() + 0.22D,
                        blockPos.getY() + 0.18D,
                        blockPos.getZ() + 0.22D,
                        blockPos.getX() + 0.78D,
                        blockPos.getY() + 0.82D,
                        blockPos.getZ() + 0.78D
                );
            }

            return AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(blockPos)).inflate(0.02D);
        }

        private static String readSkullTextureValue(BlockEntity blockEntity) {
            if (!(blockEntity instanceof SkullBlockEntity skullBlockEntity)) {
                return null;
            }

            ResolvableProfile profile = skullBlockEntity.getOwnerProfile();
            if (profile == null) {
                return null;
            }

            Collection<Property> textures = profile.properties().get("textures");
            if (textures == null || textures.isEmpty()) {
                return null;
            }

            for (Property property : textures) {
                String value = property.value();
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }

            return null;
        }
    }

    private static boolean matchesCustomModel(ItemStack stack, int modelId) {
        if (stack.isEmpty()) {
            return false;
        }

        CustomModelData customModelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (customModelData == null) {
            return false;
        }

        Float firstValue = customModelData.getFloat(0);
        return firstValue != null && Mth.floor(firstValue) == modelId;
    }

    private static String readItemModel(ItemStack stack) {
        Object itemModel = stack.get(DataComponents.ITEM_MODEL);
        return itemModel == null ? "" : itemModel.toString();
    }
}
