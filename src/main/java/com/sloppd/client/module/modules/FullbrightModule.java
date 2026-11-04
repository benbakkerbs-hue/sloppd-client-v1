package com.sloppd.client.module.modules;

import com.sloppd.client.module.ClientModule;
import com.sloppd.client.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

public final class FullbrightModule extends ClientModule {
    private double previousGamma;
    public FullbrightModule() {
        super("Fullbright", "Raises client gamma; restores the previous value when disabled.", ModuleCategory.RENDER);
    }
    @Override public void onEnable(MinecraftClient client) {
        previousGamma = client.options.getGamma().getValue();
        client.options.getGamma().setValue(16.0);
    }
    @Override public void onDisable(MinecraftClient client) {
        client.options.getGamma().setValue(previousGamma);
    }
}
