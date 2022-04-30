package de.laparudi.spawnertools.listeners;

import com.cryptomorin.xseries.XMaterial;
import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Bukkit;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class SpawnerPlaceListener implements Listener {
    
    @EventHandler
    public void onSpawnerPlace(final BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != XMaterial.SPAWNER.parseMaterial()) return;

        final Player player = event.getPlayer();
        final CreatureSpawner spawner = (CreatureSpawner) event.getBlockPlaced().getState();
        final String type = spawner.getSpawnedType().name();
        
        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> 
                SpawnerTools.getPlugin().getMySQL().createSpawner(event.getBlockPlaced().getLocation(), player.getUniqueId(), type), 4);
    }
}
