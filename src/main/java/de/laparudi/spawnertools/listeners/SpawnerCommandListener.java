package de.laparudi.spawnertools.listeners;

import com.cryptomorin.xseries.XMaterial;
import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class SpawnerCommandListener implements Listener {
    
    @EventHandler
    public void onSpawnerCommand(final PlayerCommandPreprocessEvent event) {
        if(!event.getMessage().contains("spawner") || !event.getPlayer().hasPermission("essentials.spawner")) return;
        
        final Block target = event.getPlayer().getTargetBlock(null, 5);
        if (target.getType() != XMaterial.SPAWNER.parseMaterial()) return;

        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> {
            final CreatureSpawner spawner = (CreatureSpawner) target.getState();
            SpawnerTools.getPlugin().getMySQL().setValue(spawner.getLocation(), "Type", spawner.getSpawnedType().name());
        }, 4);
    }
}
