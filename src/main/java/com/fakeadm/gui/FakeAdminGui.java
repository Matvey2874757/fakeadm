package com.fakeadm.gui;

import com.fakeadm.client.FakeAdminMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FakeAdminGui extends Screen {
    private enum Tab { PLAYERS, WORLD, FLIGHT, TELEPORT, SETTINGS }

    private Tab active = Tab.PLAYERS;
    private final Set<String> mutedPlayers = new HashSet<>();

    public FakeAdminGui() {
        super(Text.literal("FakeAdmin Demo"));
    }

    @Override
    protected void init() {
        clearChildren();
        int x = 10;
        for (Tab tab : Tab.values()) {
            addDrawableChild(ButtonWidget.builder(Text.literal(tabTitle(tab)), b -> {
                active = tab;
                init();
            }).dimensions(x, 10, 110, 20).build());
            x += 114;
        }

        switch (active) {
            case PLAYERS -> buildPlayersTab();
            case WORLD -> buildWorldTab();
            case FLIGHT -> buildFlightTab();
            case TELEPORT -> buildTeleportTab();
            case SETTINGS -> buildSettingsTab();
        }
    }

    private void buildPlayersTab() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) return;

        List<PlayerListEntry> players = client.getNetworkHandler().getListedPlayerListEntries();
        int y = 40;
        for (PlayerListEntry entry : players) {
            String name = entry.getProfile().getName();
            addDrawableChild(ButtonWidget.builder(Text.literal("Mute " + name), b -> {
                mutedPlayers.add(name);
                sendLocal("[Server] You muted " + name + " for 10 min", SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
            }).dimensions(20, y, 110, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Kick " + name), b ->
                    sendLocal("[Server] " + name + " was kicked by an operator.", SoundEvents.UI_BUTTON_CLICK)
            ).dimensions(140, y, 120, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("TP demo " + name), b ->
                    FakeAdminMod.TELEPORT.attemptRealTeleport(client, name)
            ).dimensions(270, y, 120, 20).build());
            y += 24;
            if (y > height - 30) break;
        }
    }

    private void buildWorldTab() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Fake radar"), b -> showRadar()).dimensions(20, 40, 140, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Fake /give"), b ->
                sendLocal("[Server] Gave diamond_sword * 1, but you lack permissions", SoundEvents.UI_BUTTON_CLICK)
        ).dimensions(170, 40, 180, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Open fake console"), b -> openFakeConsole()).dimensions(360, 40, 180, 20).build());
    }

    private void buildFlightTab() {
        addDrawableChild(ButtonWidget.builder(Text.literal(FakeAdminMod.FLIGHT.isEnabled() ? "Disable flight" : "Enable flight"), b -> {
            FakeAdminMod.FLIGHT.toggle(client);
            init();
        }).dimensions(20, 40, 180, 20).build());
    }

    private void buildTeleportTab() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Try real TP (demo)"), b ->
                FakeAdminMod.TELEPORT.attemptRealTeleport(client, "target")
        ).dimensions(20, 40, 180, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Packet imitation (fake)"), b -> {
            if (client.player != null) {
                Vec3d base = client.player.getPos();
                FakeAdminMod.TELEPORT.fakePacketTeleport(client, base.add(3, 0, 0));
            }
        }).dimensions(210, 40, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Delayed fake TP"), b ->
                FakeAdminMod.TELEPORT.delayedFakeTeleport(client)
        ).dimensions(420, 40, 140, 20).build());
    }

    private void buildSettingsTab() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Toggle chat filter: " + FakeAdminMod.CONFIG.enableChatFilter), b -> {
            FakeAdminMod.CONFIG.enableChatFilter = !FakeAdminMod.CONFIG.enableChatFilter;
            FakeAdminMod.CONFIG.save();
            init();
        }).dimensions(20, 40, 220, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawText(textRenderer, "Tab: " + tabTitle(active), 20, height - 25, 0xFFFFFF, true);
        context.drawText(textRenderer, "Все действия в этом моде локальные (демо)", 20, height - 14, 0x55FF55, false);
    }

    private void showRadar() {
        if (client.world == null) return;
        sendLocal("[FakeAdmin Radar] ---", SoundEvents.UI_BUTTON_CLICK);
        client.world.getPlayers().forEach(p -> sendLocal(String.format("%s xyz=(%.1f %.1f %.1f) hp=%.1f",
                p.getName().getString(), p.getX(), p.getY(), p.getZ(), p.getHealth()), SoundEvents.UI_BUTTON_CLICK));
    }

    private void openFakeConsole() {
        JFrame frame = new JFrame("FakeAdmin Console");
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setText("help -> локальные команды\\nlist -> список демонстрационных модулей\\n\\n> help\\nhelp, list\\n> list\\nplayers, world, flight, teleport, settings");
        frame.setContentPane(new JScrollPane(area));
        frame.setSize(420, 260);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void sendLocal(String msg, net.minecraft.sound.SoundEvent sound) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(msg), false);
            client.player.playSound(sound, 0.8f, 1.0f);
        }
    }

    private static String tabTitle(Tab tab) {
        return switch (tab) {
            case PLAYERS -> "Игроки";
            case WORLD -> "Мир (локальный)";
            case FLIGHT -> "Полёт";
            case TELEPORT -> "Телепорт";
            case SETTINGS -> "Настройки";
        };
    }
}
