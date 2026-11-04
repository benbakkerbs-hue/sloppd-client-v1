package com.sloppd.client;

import com.sloppd.client.gui.ClickGuiScreen;
import com.sloppd.client.module.ModuleManager;
import com.sloppd.client.module.modules.HudModules;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SloppdClient implements ClientModInitializer {
    public static final String NAME = "sloppd client";
    public static final String VERSION = "1.2.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    private static KeyBinding clickGuiKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        HudModules.registerHud();
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sloppdclient.clickgui", InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.create(Identifier.of("sloppdclient", "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clickGuiKey.wasPressed()) {
                if (client.currentScreen instanceof ClickGuiScreen) client.setScreen(null);
                else if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
            }
            ModuleManager.tick(client);
        });
        LOGGER.info("sloppd client {} initialized", VERSION);
    }
}
