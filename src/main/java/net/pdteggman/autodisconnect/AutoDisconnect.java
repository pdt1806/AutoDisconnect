package net.pdteggman.autodisconnect;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

@Environment(EnvType.CLIENT)
public class AutoDisconnect implements ClientModInitializer {
    boolean toggle = true;
    int healthToLeave = 8;
    int cooldownInSeconds = 15;
    int healthWhenDisconnected = 0;
    int cooldown = cooldownInSeconds * 20;

    File jarFile = new File(AutoDisconnect.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    File parentFolder = jarFile.getParentFile().getParentFile();
    String folderPath = parentFolder.getAbsolutePath();
    File configFile = new File("autodisconnect/config.txt");

    @Override
    public void onInitializeClient() {
        createConfigFileIfNotExists();

        try {
            Scanner scanner = new Scanner(configFile);
            toggle = scanner.nextBoolean();
            healthToLeave = scanner.nextInt();
            cooldownInSeconds = scanner.nextInt();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerCommands();
        registerDisconnectEvent();
    }

    private void createConfigFileIfNotExists() {
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                writeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeFile() {
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("%s %s %s".formatted(toggle, healthToLeave, cooldownInSeconds));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(ClientCommandManager.literal("autodisconnect")
                    .then(ClientCommandManager.literal("toggle").executes(context -> {
                        toggle = !toggle;
                        context.getSource()
                                .sendFeedback(Text.of((toggle ? "Enabled" : "Disabled") + " AutoDisconnect!"));
                        writeFile();
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("health")
                            .then(ClientCommandManager.argument("health", IntegerArgumentType.integer(1, 19))
                                    .executes(context -> {
                                        healthToLeave = IntegerArgumentType.getInteger(context, "health");
                                        context.getSource().sendFeedback(
                                                Text.of("AutoDisconnect Health changed to " + healthToLeave));
                                        writeFile();
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("cooldown")
                            .then(ClientCommandManager.argument("cooldown", IntegerArgumentType.integer(1, 60))
                                    .executes(context -> {
                                        cooldownInSeconds = IntegerArgumentType.getInteger(context, "cooldown");
                                        context.getSource().sendFeedback(Text.of("AutoDisconnect Cooldown changed to "
                                                + cooldownInSeconds + " second" + (cooldownInSeconds == 1 ? "" : "s")));
                                        writeFile();
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("status").executes(context -> {
                        context.getSource()
                                .sendFeedback(Text.of("AutoDisconnect is " + (toggle ? "Enabled" : "Disabled")));
                        context.getSource().sendFeedback(Text.of("AutoDisconnect Health is " + healthToLeave));
                        context.getSource().sendFeedback(Text.of("AutoDisconnect Cooldown is " + cooldownInSeconds
                                + " second" + (cooldownInSeconds == 1 ? "" : "s")));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("default").executes(context -> {
                        toggle = true;
                        healthToLeave = 8;
                        cooldownInSeconds = 15;
                        context.getSource().sendFeedback(Text.of("AutoDisconnect settings reset to default!"));
                        writeFile();
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("help").executes(context -> {
                        context.getSource().sendFeedback(Text.of("AutoDisconnect Commands:"));
                        context.getSource().sendFeedback(Text.of("/autodisconnect toggle - Toggles AutoDisconnect"));
                        context.getSource().sendFeedback(
                                Text.of("/autodisconnect health <health> - Sets the health to disconnect at (1-19)"));
                        context.getSource().sendFeedback(Text.of(
                                "/autodisconnect cooldown <cooldown> - Sets the cooldown (in seconds) after reconnecting to disconnect again (1-60)"));
                        context.getSource().sendFeedback(
                                Text.of("/autodisconnect status - Shows the current values of AutoDisconnect"));
                        return 1;
                    })));
        });
    }

    private void registerDisconnectEvent() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null || client.player.isCreative()
                    || client.player.isSpectator() || !toggle) {
                return;
            }

            int health = (int) client.player.getHealth();
            if (healthWhenDisconnected > 0 && health == 20) {
                return;
            }

            if (health > healthToLeave) {
                cooldown = 0;
                healthWhenDisconnected = 0;
            } else if (client.player.isAlive()) {
                if (cooldown == 0 || (healthWhenDisconnected > 0 && health < healthWhenDisconnected)) {
                    healthWhenDisconnected = health;
                    cooldown = cooldownInSeconds * 20;
                    client.world.disconnect();
                } else {
                    cooldown--;
                }
            }
        });
    }
}
