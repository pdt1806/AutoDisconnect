package net.pdteggman.autodisconnect;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.text.Text;

public class ClientCommandManager {

    public static final String DISPATCHER = null;
    public static final String EVENT = null;

    public static ArgumentBuilder<ClientCommandManager, LiteralArgumentBuilder<ClientCommandManager>> literal(
            String string) {
        return null;
    }

    public static ArgumentBuilder<ClientCommandManager, LiteralArgumentBuilder<ClientCommandManager>> argument(
            String string, DoubleArgumentType doubleArg) {
        return null;
    }

    public void sendMessage(Text of, boolean b) {
    }

}
