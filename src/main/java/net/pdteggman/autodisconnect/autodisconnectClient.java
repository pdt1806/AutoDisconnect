package net.pdteggman.autodisconnect;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerEntity;

public class autodisconnectClient implements ClientModInitializer {
    Boolean toggle = true;
    Double healthToLeave = 4.0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = client.player;
            if (player != null) {
                float health = player.getHealth();
                if (health <= healthToLeave) {
                    player.world.disconnect();
            }
        }});
    }

    public void receiveToggleValue(Boolean toggleStatus) {
        toggle = toggleStatus;
    }

    public void receiveHealthValue(Double healthValue) {
        healthToLeave = healthValue;
    }
}