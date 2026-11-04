package com.sloppd.client.module;

public enum ModuleCategory {
    MOVEMENT("Movement"), RENDER("Render"), PLAYER("Player"), WORLD("World"), MISC("Misc");

    public final String displayName;
    ModuleCategory(String displayName) { this.displayName = displayName; }
}
