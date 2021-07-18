package de.laparudi.spawnertools.listeners;

import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class SpawnerPlaceListener implements Listener {
    
    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        if(event.getBlockPlaced().getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();
        CreatureSpawner spawner = (CreatureSpawner) event.getBlockPlaced().getState();
        String type = spawner.getSpawnedType().name();
        
        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> 
                SpawnerTools.getPlugin().getMySQL().createSpawner(event.getBlockPlaced().getLocation(), player.getUniqueId(), type), 4);
    }
}
