package com.sloppd.client.module.modules;

import com.sloppd.client.module.ClientModule;
import com.sloppd.client.module.ModuleCategory;
import com.sloppd.client.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21.11 renderer port.
 *
 * The newer Fabric world-render API splits frame extraction from drawing.
 * World data is read only in END_EXTRACTION. The drawing callback consumes
 * an immutable snapshot and does not query ClientWorld.
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

        if (world == null || player == null) {
            frame = FrameSnapshot.EMPTY;
            return;
        }

        boolean entityEsp = enabled("Entity ESP");
        boolean storageEsp = enabled("Storage ESP");
        boolean blockEsp = enabled("Block ESP");
        boolean tracers = enabled("Tracers");

        if (!entityEsp && !storageEsp && !blockEsp && !tracers) {
            frame = FrameSnapshot.EMPTY;
            return;
        }

        Vec3d camera = context.camera().getPos();
        List<Box> entityBoxes = new ArrayList<>();
        List<Box> storageBoxes = new ArrayList<>();
        List<Box> oreBoxes = new ArrayList<>();
        List<Vec3d> tracerTargets = new ArrayList<>();

        if (entityEsp || tracers) {
            for (Entity entity : world.getEntities()) {
                if (entity == player || !(entity instanceof LivingEntity living) || !living.isAlive()) {
                    continue;
                }
                if (entityEsp) {
                    entityBoxes.add(entity.getBoundingBox().expand(0.035));
                }
                if (tracers) {
                    tracerTargets.add(entity.getPos().add(0.0, entity.getHeight() * 0.5, 0.0));
                }
            }
        }

        if (storageEsp) {
            BlockPos playerPos = player.getBlockPos();
            for (var blockEntity : world.getBlockEntities()) {
                BlockPos pos = blockEntity.getPos();
                if (pos.getSquaredDistance(playerPos) > 48.0 * 48.0) continue;

                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof ChestBlock
                        || state.getBlock() instanceof EnderChestBlock
                        || state.getBlock() instanceof ShulkerBoxBlock) {
                    Box box = state.getOutlineShape(world, pos).getBoundingBox().offset(pos);
                    storageBoxes.add(box);
                }
            }
        }

        if (blockEsp) {
            BlockPos center = player.getBlockPos();
            int radius = 10;
            for (BlockPos pos : BlockPos.iterate(
                    center.add(-radius, -radius, -radius),
                    center.add(radius, radius, radius))) {
                BlockState state = world.getBlockState(pos);
                if (state.isOf(Blocks.DIAMOND_ORE)
                        || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)
                        || state.isOf(Blocks.EMERALD_ORE)
                        || state.isOf(Blocks.DEEPSLATE_EMERALD_ORE)
                        || state.isOf(Blocks.ANCIENT_DEBRIS)) {
                    oreBoxes.add(new Box(pos).contract(0.01));
                }
            }
        }

        frame = new FrameSnapshot(
                camera.x, camera.y, camera.z,
                List.copyOf(entityBoxes),
                List.copyOf(storageBoxes),
                List.copyOf(oreBoxes),
                List.copyOf(tracerTargets)
        );
    }

    private static void drawFrame(WorldRenderContext context) {
        FrameSnapshot current = frame;
        if (current == FrameSnapshot.EMPTY) return;

        boolean entityEsp = enabled("Entity ESP");
        boolean storageEsp = enabled("Storage ESP");
        boolean blockEsp = enabled("Block ESP");
        boolean tracers = enabled("Tracers");

        if (!entityEsp && !storageEsp && !blockEsp && !tracers) return;

        MatrixStack matrices = context.matrices();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        matrices.push();
        matrices.translate(-current.cameraX, -current.cameraY, -current.cameraZ);
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());

        if (entityEsp) {
            for (Box box : current.entityBoxes) {
                WorldRenderer.drawBox(matrices, lines, box,
                        0.82F, 0.38F, 1.0F, 0.95F);
            }
        }

        if (storageEsp) {
            for (Box box : current.storageBoxes) {
                WorldRenderer.drawBox(matrices, lines, box,
                        1.0F, 0.72F, 0.18F, 1.0F);
            }
        }

        if (blockEsp) {
            for (Box box : current.oreBoxes) {
                WorldRenderer.drawBox(matrices, lines, box,
                        0.20F, 0.95F, 0.82F, 1.0F);
            }
        }

        if (tracers) {
            for (Vec3d target : current.tracerTargets) {
                Vec3d relative = target.subtract(
                        current.cameraX, current.cameraY, current.cameraZ);
                drawLine(matrices, lines, Vec3d.ZERO, relative,
                        0.78F, 0.38F, 1.0F, 0.95F);
            }
        }

        matrices.pop();
    }

    private static boolean enabled(String name) {
        ClientModule module = ModuleManager.get(name);
        return module != null && module.isEnabled();
    }

    private static void drawLine(MatrixStack matrices, VertexConsumer consumer,
                                 Vec3d from, Vec3d to,
                                 float red, float green, float blue, float alpha) {
        MatrixStack.Entry entry = matrices.peek();
        consumer.vertex(entry.getPositionMatrix(), (float) from.x, (float) from.y, (float) from.z)
                .color(red, green, blue, alpha)
                .normal(entry, 0.0F, 1.0F, 0.0F);
        consumer.vertex(entry.getPositionMatrix(), (float) to.x, (float) to.y, (float) to.z)
                .color(red, green, blue, alpha)
                .normal(entry, 0.0F, 1.0F, 0.0F);
    }

    private record FrameSnapshot(
            double cameraX, double cameraY, double cameraZ,
            List<Box> entityBoxes,
            List<Box> storageBoxes,
            List<Box> oreBoxes,
            List<Vec3d> tracerTargets
    ) {
        private static final FrameSnapshot EMPTY =
                new FrameSnapshot(0, 0, 0, List.of(), List.of(), List.of(), List.of());
    }

    private abstract static class VisualBase extends ClientModule {
        protected VisualBase(String name, String description) {
            super(name, description, ModuleCategory.RENDER);
            registerRenderer();
        }
    }

    public static final class EntityEsp extends VisualBase {
        public EntityEsp() {
            super("Entity ESP", "Outlines nearby living entities.");
        }
    }

    public static final class StorageEsp extends VisualBase {
        public StorageEsp() {
            super("Storage ESP", "Highlights loaded chests and shulker boxes.");
        }
    }

    public static final class BlockEsp extends VisualBase {
        public BlockEsp() {
            super("Block ESP", "Highlights selected ores and ancient debris nearby.");
        }
    }

    public static final class Tracers extends VisualBase {
        public Tracers() {
            super("Tracers", "Draws lines from the camera to nearby living entities.");
        }
    }
}