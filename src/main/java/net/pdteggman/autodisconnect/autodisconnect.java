package net.pdteggman.autodisconnect;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.*;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class autodisconnect implements ModInitializer {
    Boolean toggle = true;
    Double health = 4.0;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("autodisconnect").then(literal("toggle").executes(context -> {
            toggle = !toggle;
            context.getSource().sendMessage(Text.literal("AutoDisconnect Status: " + (toggle ? "Enabled" : "Disabled")));
            return 1;
        }))
        .then(literal("health")).then(argument("health", DoubleArgumentType.doubleArg())).executes(context -> {
            health = DoubleArgumentType.getDouble(context, "health");
            return 1;
        })));
    };

    public void sendToggleValue() {
        autodisconnectClient receiver = new autodisconnectClient();
        receiver.receiveToggleValue(toggle);
    }

    public void receiveHealthValue() {
        autodisconnectClient receiver = new autodisconnectClient();
        receiver.receiveHealthValue(health);
    }
}