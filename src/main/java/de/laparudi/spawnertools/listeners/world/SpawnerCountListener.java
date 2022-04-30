package de.laparudi.spawnertools.listeners.world;

import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class SpawnerCountListener implements Listener {
    
    @EventHandler
    public void onSpawn(final SpawnerSpawnEvent event) {
        final Location location = event.getSpawner().getLocation();
        if (!SpawnerTools.getPlugin().getMySQL().spawnerExists(location)) {
            return;
        }
        
        final int newValue = SpawnerTools.getPlugin().getMySQL().getIntValue(location, "Spawns") +1;
        SpawnerTools.getPlugin().getMySQL().setIntValue(location, "Spawns", newValue);
    }
}
