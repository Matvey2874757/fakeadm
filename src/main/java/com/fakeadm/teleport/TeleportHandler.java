package com.fakeadm.teleport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class TeleportHandler {
    private int delayedTicks = -1;

    public void attemptRealTeleport(MinecraftClient client, String target) {
        if (client.player == null) return;
        effects(client);
        client.player.sendMessage(Text.literal("[FakeAdmin] Сервер отклонил телепорт (нет прав): " + target), false);
    }

    public void fakePacketTeleport(MinecraftClient client, Vec3d target) {
        if (client.player == null) return;
        effects(client);
        Vec3d oldPos = client.player.getPos();
        client.player.setPosition(target);
        client.player.sendMessage(Text.literal("[FakeAdmin] Пакетная имитация телепорта (клиентский фейк)"), false);

        client.execute(() -> {
            if (client.player != null) {
                client.player.setPosition(oldPos);
                client.player.sendMessage(Text.literal("[FakeAdmin] Телепорт отменён сервером (демо-rollback)"), false);
            }
        });
    }

    public void delayedFakeTeleport(MinecraftClient client) {
        if (client.player == null) return;
        delayedTicks = 60;
        client.player.sendMessage(Text.literal("[FakeAdmin] Телепорт через 3 секунды..."), false);
    }

    public void tick(MinecraftClient client) {
        if (delayedTicks < 0 || client.player == null) {
            return;
        }
        delayedTicks--;
        if (delayedTicks == 0) {
            effects(client);
            client.player.sendMessage(Text.literal("[FakeAdmin] Ошибка: недостаточно прав"), false);
            delayedTicks = -1;
        }
    }

    private void effects(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        client.player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        for (int i = 0; i < 16; i++) {
            client.world.addParticle(ParticleTypes.PORTAL,
                    client.player.getX(),
                    client.player.getBodyY(0.5),
                    client.player.getZ(),
                    (client.world.random.nextDouble() - 0.5) * 0.2,
                    client.world.random.nextDouble() * 0.2,
                    (client.world.random.nextDouble() - 0.5) * 0.2);
        }
    }
}
