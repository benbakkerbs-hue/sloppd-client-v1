package com.sloppd.client.module.modules;

import com.sloppd.client.module.ClientModule;
import com.sloppd.client.module.ModuleCategory;
import com.sloppd.client.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShapes;

import java.util.ArrayList;
import java.util.List;

/**
 * Safe visual test helpers for Minecraft 1.21.11.
 * The test marker is a fixed dummy position 10 blocks below the player.
 * It does not inspect blocks, containers, entities, or other hidden world data.
 */
public final class VisualModules {
    private static boolean registered;
    private static volatile FrameSnapshot frame = FrameSnapshot.EMPTY;

    private VisualModules() {}

    private static void registerRenderer() {
        if (registered) return;
        registered = true;
        WorldRenderEvents.END_EXTRACTION.register(VisualModules::extractFrame);
        WorldRenderEvents.AFTER_ENTITIES.register(VisualModules::drawFrame);
    }

    private static void extractFrame(WorldExtractionContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        var world = context.world();
        var player = client.player;

        if (world == null || player == null || !client.isInSingleplayer()) {
            frame = FrameSnapshot.EMPTY;
            return;
        }

        List<Box> markerBoxes = new ArrayList<>();
        List<Box> chunkBoxes = new ArrayList<>();

        if (enabled("Test Chest Marker")) {
            // Fixed dummy target: 10 blocks directly below the player's feet.
            // This deliberately does not check whether a chest exists there.
            BlockPos testPos = player.getBlockPos().down(10);
            markerBoxes.add(new Box(testPos));
        }

        if (enabled("Chunk Borders")) {
            BlockPos playerPos = player.getBlockPos();
            int chunkX = playerPos.getX() >> 4;
            int chunkZ = playerPos.getZ() >> 4;
            chunkBoxes.add(new Box(chunkX * 16.0, player.getY() - 1.0, chunkZ * 16.0,
                    (chunkX + 1) * 16.0, player.getY() + 8.0, (chunkZ + 1) * 16.0));
        }

        var camera = context.camera().getCameraPos();
        frame = new FrameSnapshot(camera.x, camera.y, camera.z,
                List.copyOf(markerBoxes), List.copyOf(chunkBoxes));
    }

    private static void drawFrame(WorldRenderContext context) {
        FrameSnapshot current = frame;
        if (current == FrameSnapshot.EMPTY) return;
        if (!enabled("Test Chest Marker") && !enabled("Chunk Borders")) return;

        MatrixStack matrices = context.matrices();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        matrices.push();
        matrices.translate(-current.cameraX, -current.cameraY, -current.cameraZ);
        VertexConsumer lines = consumers.getBuffer(RenderLayers.lines());

        if (enabled("Test Chest Marker")) {
            for (Box box : current.markerBoxes) {
                drawBoxOutline(matrices, lines, box, 0xFFFFB82E);
            }
        }
        if (enabled("Chunk Borders")) {
            for (Box box : current.chunkBoxes) {
                drawBoxOutline(matrices, lines, box, 0xFF55DDEE);
            }
        }
        matrices.pop();
    }

    private static void drawBoxOutline(MatrixStack matrices, VertexConsumer lines,
                                       Box box, int color) {
        VertexRendering.drawOutline(matrices, lines, VoxelShapes.cuboid(box),
                0.0, 0.0, 0.0, color, 1.0F);
    }

    private static boolean enabled(String name) {
        ClientModule module = ModuleManager.get(name);
        return module != null && module.isEnabled();
    }

    private record FrameSnapshot(double cameraX, double cameraY, double cameraZ,
                                 List<Box> markerBoxes, List<Box> chunkBoxes) {
        private static final FrameSnapshot EMPTY =
                new FrameSnapshot(0, 0, 0, List.of(), List.of());
    }

    private abstract static class VisualBase extends ClientModule {
        protected VisualBase(String name, String description) {
            super(name, description, ModuleCategory.RENDER);
            registerRenderer();
        }
    }

    public static final class TestChestMarker extends VisualBase {
        public TestChestMarker() {
            super("Test Chest Marker",
                    "Singleplayer-only dummy outline 10 blocks below you; does not detect chests.");
        }
    }

    public static final class ChunkBorders extends VisualBase {
        public ChunkBorders() {
            super("Chunk Borders", "Outlines the chunk containing the player.");
        }
    }
}
