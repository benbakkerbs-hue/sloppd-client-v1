package com.sloppd.client.module;

import com.sloppd.client.module.modules.FullbrightModule;
import com.sloppd.client.module.modules.SprintModule;
import com.sloppd.client.module.modules.VisualModules;
import com.sloppd.client.module.modules.HudModules;
import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private static final List<ClientModule> MODULES = new ArrayList<>();
    private static boolean initialized;
    private ModuleManager() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        register(new SprintModule());
        register(new FullbrightModule());
        register(new VisualModules.TestChestMarker());
        register(new VisualModules.ChunkBorders());
        register(new HudModules.Keystrokes());
        register(new HudModules.ArmorHud());
        register(new HudModules.PotionHud());
        register(new HudModules.MovementHud());
        register(new HudModules.SprintHud());
        register(new HudModules.ChunkCoordinates());
        register(new HudModules.ChunkActivity());
        register(new HudModules.PrimeSusChunk());
    }

    public static void register(ClientModule module) { MODULES.add(module); }
    public static List<ClientModule> all() { return Collections.unmodifiableList(MODULES); }
    public static List<ClientModule> byCategory(ModuleCategory category) {
        return MODULES.stream().filter(m -> m.getCategory() == category).toList();
    }
    public static ClientModule get(String name) {
        return MODULES.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
    public static void tick(MinecraftClient client) {
        for (ClientModule module : MODULES) if (module.isEnabled()) module.onTick(client);
    }
}
