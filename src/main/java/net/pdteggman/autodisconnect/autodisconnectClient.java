package net.pdteggman.autodisconnect;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class AutoDisconnectClient implements ClientModInitializer {
    Boolean toggle = true;
    Double healthToLeave = 4.0;
    Boolean gotDisconnected = false;
    Integer cooldown = 15;

    @Override
    public void onInitializeClient() {

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autodisconnect").then(ClientCommandManager.literal("toggle").executes(context -> {
                toggle = !toggle;
                context.getSource().sendFeedback(Text.of((toggle ? "Enabled" : "Disabled") + " AutoDisconnect."));
                return 1;
            })));
            dispatcher.register(ClientCommandManager.literal("autodisconnect").then(ClientCommandManager.literal("health").then(ClientCommandManager.argument("health", DoubleArgumentType.doubleArg(1, 19)).executes(context -> {
                healthToLeave = DoubleArgumentType.getDouble(context, "health");
                context.getSource().sendFeedback(Text.of("AutoDisconnect Health changed to " + healthToLeave));
                return 1;
            }))));
        });

        // ClientCommandManager.DISPATCHER.register(
        //     ClientCommandManager.literal("autodisconnect")
        //         .then(ClientCommandManager.literal("toggle").executes(context -> {
        //             toggle = !toggle;
        //             MinecraftClient.getInstance().player.sendMessage(Text.of("AutoDisconnect Status: " + (toggle ? "Enabled" : "Disabled")), false);
        //             return 1;
        //         }))
        //         .then(ClientCommandManager.literal("health")
        //             .then(ClientCommandManager.argument("health", DoubleArgumentType.doubleArg(1, 19))
        //                 .executes(context -> {
        //                     healthToLeave = DoubleArgumentType.getDouble(context, "health");
        //                     return 1;
        //                 }))
        //         )
        // );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                return;
            }
            if (client.player != null) {
                float health = client.player.getHealth();
                if (health <= healthToLeave && client.player.isAlive() && !gotDisconnected && toggle) {
                    gotDisconnected = true;
                    System.out.println(gotDisconnected);
                    client.player.world.disconnect();
                }
            }
        });
    
    }

    // public final class ClientCommandManager {

    //     public static final String DISPATCHER = null;

    //     public static Object literal(String string) {
    //         return null;
    //     }}
    //     ClientCommandManager.DISPATCHER.register(
    //         ClientCommandManager.literal("autodisconnect")
    //             .then(ClientCommandManager.literal("toggle").executes(context -> {
    //                 toggle = !toggle;
    //                 MinecraftClient.getInstance().player.sendMessage(Text.of("AutoDisconnect Status: " + (toggle ? "Enabled" : "Disabled")), false);
    //                 return 1;
    //             }))
    //             .then(ClientCommandManager.literal("health")
    //                 .then(ClientCommandManager.argument("health", DoubleArgumentType.doubleArg(1, 19))
    //                     .executes(context -> {
    //                         healthToLeave = DoubleArgumentType.getDouble(context, "health");
    //                         return 1;
    //                     }))
    //             )
    //     );

}
