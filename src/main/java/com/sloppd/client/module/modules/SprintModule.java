package com.sloppd.client.module.modules;

import com.sloppd.client.module.ClientModule;
import com.sloppd.client.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

public final class SprintModule extends ClientModule {
    public SprintModule() {
        super("Sprint", "Automatically sprints while moving forward.", ModuleCategory.MOVEMENT);
    }
    @Override public void onTick(MinecraftClient client) {
        if (client.player == null || client.currentScreen != null) return;
        if (client.player.forwardSpeed > 0.0F && !client.player.isSneaking()) {
            client.player.setSprinting(true);
        }
    }
}
