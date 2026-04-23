package com.fakeadm.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @ModifyArg(
            method = "onGameMessage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;onChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageType$Parameters;)V"),
            index = 0
    )
    private Text fakeadm$replacePermissionMessage(Text original) {
        String raw = original.getString();
        if (raw.contains("You do not have permission to use that command")) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.7f);
            }
            return Text.literal("[FakeAdmin] Команда недоступна в демо-режиме");
        }

        return original;
    }
}
