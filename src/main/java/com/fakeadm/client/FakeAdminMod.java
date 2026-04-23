package com.fakeadm.client;

import com.fakeadm.config.FakeAdminConfig;
import com.fakeadm.flight.FlightHandler;
import com.fakeadm.gui.FakeAdminGui;
import com.fakeadm.teleport.TeleportHandler;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class FakeAdminMod implements ClientModInitializer {
    public static final String MOD_ID = "fakeadm";
    public static FakeAdminConfig CONFIG;
    public static FlightHandler FLIGHT;
    public static TeleportHandler TELEPORT;

    private KeyBinding openGuiKey;
    private KeyBinding flightKey;

    @Override
    public void onInitializeClient() {
        CONFIG = FakeAdminConfig.load();
        FLIGHT = new FlightHandler();
        TELEPORT = new TeleportHandler();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fakeadm.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.fakeadm"
        ));

        flightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fakeadm.flight_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.fakeadm"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                client.setScreen(new FakeAdminGui());
                ping(client, Text.literal("[FakeAdmin] Открыта демонстрационная панель"));
            }
            while (flightKey.wasPressed()) {
                FLIGHT.toggle(client);
            }
            FLIGHT.tick(client);
            TELEPORT.tick(client);
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("fa").executes(this::openViaCommand))
        );
    }

    private int openViaCommand(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> client.setScreen(new FakeAdminGui()));
        ping(client, Text.literal("[FakeAdmin] Открытие GUI через /fa"));
        return Command.SINGLE_SUCCESS;
    }

    public static void ping(MinecraftClient client, Text text) {
        if (client.player != null) {
            client.player.sendMessage(text, false);
            client.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5f, 1.1f);
        }
    }
}
