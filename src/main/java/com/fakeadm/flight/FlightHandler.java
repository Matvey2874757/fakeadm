package com.fakeadm.flight;

import com.fakeadm.client.FakeAdminMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class FlightHandler {
    private boolean enabled;
    private int ticks;

    public void toggle(MinecraftClient client) {
        enabled = !enabled;
        ticks = 0;
        if (client.player != null) {
            client.player.sendMessage(Text.literal(enabled
                    ? "[FakeAdmin] Полёт-демо включён (локальная имитация)"
                    : "[FakeAdmin] Полёт-демо выключен"), false);
            client.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.7f, enabled ? 1.2f : 0.8f);
        }
    }

    public void tick(MinecraftClient client) {
        if (!enabled || !FakeAdminMod.CONFIG.enableFlightDemo) {
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        ticks++;
        boolean pauseTick = FakeAdminMod.CONFIG.antiAntiCheatSleepDemo && ticks % 30 == 0;
        if (pauseTick) {
            player.setVelocity(player.getVelocity().x, -0.05, player.getVelocity().z);
            return;
        }

        double vertical = 0.0;
        if (client.options.jumpKey.isPressed()) {
            vertical += 0.2;
        }
        if (client.options.sneakKey.isPressed()) {
            vertical -= 0.2;
        }

        vertical = Math.max(-1.0, Math.min(1.0, vertical));
        Vec3d velocity = player.getVelocity();
        player.setVelocity(velocity.x, vertical, velocity.z);
        player.velocityModified = true;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
