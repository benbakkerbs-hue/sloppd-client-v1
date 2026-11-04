package com.sloppd.client.module;

import net.minecraft.client.MinecraftClient;

public abstract class ClientModule {
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled;

    protected ClientModule(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public ModuleCategory getCategory() { return category; }
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean value, MinecraftClient client) {
        if (enabled == value) return;
        enabled = value;
        if (value) onEnable(client); else onDisable(client);
    }

    public void toggle(MinecraftClient client) { setEnabled(!enabled, client); }
    public void onEnable(MinecraftClient client) {}
    public void onDisable(MinecraftClient client) {}
    public void onTick(MinecraftClient client) {}
}
