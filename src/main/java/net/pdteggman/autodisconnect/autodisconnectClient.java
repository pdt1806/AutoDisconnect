package net.pdteggman.autodisconnect;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;

public class AutoDisconnectClient implements ClientModInitializer {
    Boolean toggle = true;
    Double healthToLeave = 4.0;
    Boolean gotDisconnected = false;
    Integer cooldown = 15;

    @Override
    public void onInitializeClient() {

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autodisconnect")
                .then(ClientCommandManager.literal("toggle").executes(context -> {
                    toggle = !toggle;
                    context.getSource().sendFeedback(Text.of((toggle ? "Enabled" : "Disabled") + " AutoDisconnect!"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("health")
                    .then(ClientCommandManager.argument("health", DoubleArgumentType.doubleArg(1, 19)).executes(context -> {
                        healthToLeave = DoubleArgumentType.getDouble(context, "health");
                        context.getSource().sendFeedback(Text.of("AutoDisconnect Health changed to " + healthToLeave));
                        return 1;
                    }))
                )
                .then(ClientCommandManager.literal("cooldown")
                    .then(ClientCommandManager.argument("cooldown", IntegerArgumentType.integer(1, 60)).executes(context -> {
                        cooldown = IntegerArgumentType.getInteger(context, "cooldown");
                        context.getSource().sendFeedback(Text.of("AutoDisconnect Cooldown changed to " + cooldown + " seconds"));
                        return 1;
                    })))
                .then(ClientCommandManager.literal("status").executes(context -> {
                    context.getSource().sendFeedback(Text.of("AutoDisconnect is " + (toggle ? "Enabled" : "Disabled")));
                    context.getSource().sendFeedback(Text.of("AutoDisconnect Health is " + healthToLeave));
                    context.getSource().sendFeedback(Text.of("AutoDisconnect Cooldown is " + cooldown + " seconds"));
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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                return;
            }
            if (client.player != null) {
                float health = client.player.getHealth();
                if (health <= healthToLeave && client.player.isAlive() && !gotDisconnected && toggle) {
                    gotDisconnected = true;
                    client.player.world.disconnect();
                }
            }
        });
    
    }

}
