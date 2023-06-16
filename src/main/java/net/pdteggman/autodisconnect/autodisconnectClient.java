package net.pdteggman.autodisconnect;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;

public class AutoDisconnectClient implements ClientModInitializer {
    boolean toggle = true;
    int healthToLeave = 8;
    int healthWhenDisconnected = 0;
    int cooldownInSeconds = 15;
    int cooldown = cooldownInSeconds * 20;

    @Override
    public void onInitializeClient() {
        Commands();
        Disconnect();
    }

    public void Commands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(ClientCommandManager.literal("autodisconnect")
                .then(ClientCommandManager.literal("toggle").executes(context -> {
                    toggle = !toggle;
                    context.getSource().sendFeedback(Text.of((toggle ? "Enabled" : "Disabled") + " AutoDisconnect!"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("health")
                    .then(ClientCommandManager.argument("health", IntegerArgumentType.integer(1, 19)).executes(context -> {
                        healthToLeave = IntegerArgumentType.getInteger(context, "health");
                        context.getSource().sendFeedback(Text.of("AutoDisconnect Health changed to " + healthToLeave));
                        return 1;
                    }))
                )
                .then(ClientCommandManager.literal("cooldown")
                    .then(ClientCommandManager.argument("cooldown", IntegerArgumentType.integer(1, 60)).executes(context -> {
                        cooldownInSeconds = IntegerArgumentType.getInteger(context, "cooldown");
                        context.getSource().sendFeedback(Text.of("AutoDisconnect Cooldown changed to " + cooldownInSeconds + " seconds"));
                        return 1;
                    })))
                .then(ClientCommandManager.literal("status").executes(context -> {
                    context.getSource().sendFeedback(Text.of("AutoDisconnect is " + (toggle ? "Enabled" : "Disabled")));
                    context.getSource().sendFeedback(Text.of("AutoDisconnect Health is " + healthToLeave));
                    context.getSource().sendFeedback(Text.of("AutoDisconnect Cooldown is " + cooldownInSeconds + " seconds"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("help").executes(context -> {
                    context.getSource().sendFeedback(Text.of("AutoDisconnect Commands:"));
                    context.getSource().sendFeedback(Text.of("/autodisconnect toggle - Toggles AutoDisconnect"));
                    context.getSource().sendFeedback(Text.of("/autodisconnect health <health> - Sets the health to disconnect at (1-19)"));
                    context.getSource().sendFeedback(Text.of("/autodisconnect cooldown <cooldown> - Sets the cooldown (in seconds) after reconnecting to disconnect again (1-60)"));
                    context.getSource().sendFeedback(Text.of("/autodisconnect settings - Shows the current settings of AutoDisconnect"));
                    return 1;
                }))
            );
        });
    }

    public void Disconnect() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) {
                return;
            }
            int health = (int) client.player.getHealth();
            if (healthWhenDisconnected > 0 && health == 20) {
                return;
            }
            if (health > healthToLeave) {
                cooldown = 0;
                healthWhenDisconnected = 0;
            } else if (health <= healthToLeave && client.player.isAlive() && toggle) {
                if (cooldown == 0 || (healthWhenDisconnected > 0 && health < healthWhenDisconnected)) {
                    healthWhenDisconnected = health;
                    cooldown = cooldownInSeconds * 20;
                    client.player.world.disconnect();               
                } else {
                    cooldown--;
                }
            }
        });
    }
}