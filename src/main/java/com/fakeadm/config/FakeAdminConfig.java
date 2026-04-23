package com.fakeadm.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FakeAdminConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fakeadm.json");

    public int openGuiKey = 82; // GLFW_KEY_R
    public int flightKey = 71; // GLFW_KEY_G
    public boolean enableChatFilter = true;
    public boolean enableFlightDemo = true;
    public boolean enableTeleportDemo = true;
    public boolean antiAntiCheatSleepDemo = true;

    public static FakeAdminConfig load() {
        if (Files.exists(PATH)) {
            try {
                return GSON.fromJson(Files.readString(PATH), FakeAdminConfig.class);
            } catch (Exception ignored) {
            }
        }
        FakeAdminConfig config = new FakeAdminConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException ignored) {
        }
    }
}
