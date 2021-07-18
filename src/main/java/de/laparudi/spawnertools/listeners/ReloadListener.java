package de.laparudi.spawnertools.listeners;

import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ReloadListener implements Listener {

    @EventHandler
    public void onChat(PlayerCommandPreprocessEvent event) {
        if(event.getMessage().contains("reload") && event.getMessage().contains("spawnertools")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(SpawnerTools.getPlugin().prefix + "§cSpawnerTools sollte nicht reloaded werden. Starte den Server neu.");
        }
    }
}
