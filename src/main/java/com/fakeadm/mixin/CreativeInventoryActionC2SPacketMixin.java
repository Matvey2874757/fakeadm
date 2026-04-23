package com.fakeadm.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class CreativeInventoryActionC2SPacketMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void fakeadm$blockCreative(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof CreativeInventoryActionC2SPacket) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cФейк-админка: нельзя брать предметы"), false);
                client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.6f);
            }
            ci.cancel();
        }
    }
}
