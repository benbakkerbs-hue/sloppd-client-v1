package com.sloppd.client.module.modules;

import com.sloppd.client.module.ClientModule;
import com.sloppd.client.module.ModuleCategory;
import com.sloppd.client.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import net.minecraft.entity.EquipmentSlot;

public final class HudModules {
    private static boolean registered;

    private HudModules() {}

    public static void registerHud() {
        if (registered) return;
        registered = true;
        HudElementRegistry.addLast(Identifier.of("sloppdclient", "sloppd_hud"),
                (context, tickCounter) -> render(context));
    }

    private static boolean enabled(String name) {
        ClientModule module = ModuleManager.get(name);
        return module != null && module.isEnabled();
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;

        int x = 8;
        int y = 8;
        int white = 0xFFFFFFFF;
        int accent = 0xFFB783FF;
        if (enabled("Keystrokes")) {
            var options = client.options;
            String keys = (options.forwardKey.isPressed() ? "[W]" : " W ")
                    + (options.leftKey.isPressed() ? "[A]" : " A ")
                    + (options.backKey.isPressed() ? "[S]" : " S ")
                    + (options.rightKey.isPressed() ? "[D]" : " D ")
                    + (options.jumpKey.isPressed() ? " [SPACE]" : "  SPACE ");
            y = line(context, "KEYS " + keys, x, y, white);
        }
        if (enabled("Armor HUD")) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot slot : slots) {
                var stack = client.player.getEquippedStack(slot);
                String value = stack.isEmpty() ? "empty" :
                        stack.getName().getString() + " " + (stack.isDamageable()
                                ? Math.max(0, stack.getMaxDamage() - stack.getDamage()) : "OK");
                y = line(context, "ARMOR " + slot.getName() + ": " + value, x, y, white);
            }
        }
        if (enabled("Potion HUD")) {
            for (var effect : client.player.getStatusEffects()) {
                String name = effect.getEffectType().value().getName().getString();
                y = line(context, "EFFECT " + name + "  " + formatTicks(effect.getDuration()), x, y, white);
            }
            if (client.player.getStatusEffects().isEmpty()) y = line(context, "EFFECTS none", x, y, white);
        }
        if (enabled("Movement HUD")) {
            double speed = client.player.getVelocity().horizontalLength() * 20.0;
            y = line(context, String.format("SPEED %.2f blocks/s", speed), x, y, white);
        }
        if (enabled("Sprint HUD")) {
            y = line(context, "SPRINT " + (client.player.isSprinting() ? "ON" : "OFF"), x, y, white);
        }
        if (enabled("Chunk Coordinates") || enabled("Chunk Activity") || enabled("Prime Sus Chunk")) {
            int chunkX = client.player.getBlockPos().getX() >> 4;
            int chunkZ = client.player.getBlockPos().getZ() >> 4;
            if (enabled("Chunk Coordinates")) {
                y = line(context, "CHUNK " + chunkX + ", " + chunkZ, x, y, accent);
            }
            if (enabled("Chunk Activity")) {
                int blockEntities = 0;
                for (var blockEntity : client.world.getBlockEntities()) {
                    if ((blockEntity.getPos().getX() >> 4) == chunkX
                            && (blockEntity.getPos().getZ() >> 4) == chunkZ) blockEntities++;
                }
                y = line(context, "CHUNK DATA block entities: " + blockEntities, x, y, white);
            }
            if (enabled("Prime Sus Chunk")) {
                if (!client.isInSingleplayer()) {
                    y = line(context, "PRIME SUS CHUNK: singleplayer only", x, y, 0xFFFFC266);
                } else {
                    int containers = 0;
                    for (var blockEntity : client.world.getBlockEntities()) {
                        if ((blockEntity.getPos().getX() >> 4) != chunkX
                                || (blockEntity.getPos().getZ() >> 4) != chunkZ) continue;
                        var state = client.world.getBlockState(blockEntity.getPos());
                        String block = state.getBlock().getTranslationKey();
                        if (block.contains("chest") || block.contains("barrel")
                                || block.contains("shulker_box") || block.contains("hopper")) containers++;
                    }
                    String status = containers >= 4 ? "unusual container density (" + containers + ")"
                            : "no unusual container density (" + containers + ")";
                    y = line(context, "PRIME SUS CHUNK: " + status, x, y,
                            containers >= 4 ? 0xFFFFC266 : white);
                }
            }
        }
    }

    private static int line(DrawContext context, String value, int x, int y, int color) {
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.literal(value), x, y, color);
        return y + 11;
    }

    private static String formatTicks(int ticks) {
        if (ticks < 0) return "∞";
        int seconds = ticks / 20;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    public static final class Keystrokes extends ClientModule {
        public Keystrokes() { super("Keystrokes", "Shows movement and jump key states.", ModuleCategory.PLAYER); }
    }
    public static final class ArmorHud extends ClientModule {
        public ArmorHud() { super("Armor HUD", "Shows equipped armor and remaining durability.", ModuleCategory.PLAYER); }
    }
    public static final class PotionHud extends ClientModule {
        public PotionHud() { super("Potion HUD", "Shows active effect names and durations.", ModuleCategory.PLAYER); }
    }
    public static final class MovementHud extends ClientModule {
        public MovementHud() { super("Movement HUD", "Shows current horizontal movement speed.", ModuleCategory.MOVEMENT); }
    }
    public static final class SprintHud extends ClientModule {
        public SprintHud() { super("Sprint HUD", "Shows whether sprint is currently active.", ModuleCategory.MOVEMENT); }
    }
    public static final class ChunkCoordinates extends ClientModule {
        public ChunkCoordinates() { super("Chunk Coordinates", "Shows the current chunk coordinates.", ModuleCategory.WORLD); }
    }
    public static final class ChunkActivity extends ClientModule {
        public ChunkActivity() { super("Chunk Activity", "Shows block-entity count in the current loaded chunk.", ModuleCategory.WORLD); }
    }
    public static final class PrimeSusChunk extends ClientModule {
        public PrimeSusChunk() { super("Prime Sus Chunk", "In singleplayer, flags unusually dense container patterns in the current chunk.", ModuleCategory.WORLD); }
    }
}
