package de.laparudi.spawnertools.listeners;

import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

public class SpawnerCommandListener implements Listener {
    
    @EventHandler
    public void onSpawnerCommand(PlayerCommandPreprocessEvent event) {
        if(!event.getMessage().contains("spawner") || !event.getPlayer().hasPermission("essentials.spawner")) {
            return;
        }
        
        Block target = event.getPlayer().getTargetBlock((Set<Material>) null, 5);
        if(target.getType() != Material.MOB_SPAWNER) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> {
            CreatureSpawner spawner = (CreatureSpawner) target.getState();
            SpawnerTools.getPlugin().getMySQL().setValue(spawner.getLocation(), "Type", spawner.getSpawnedType().name());
        }, 4);
    }
}
