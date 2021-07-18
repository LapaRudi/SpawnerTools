package de.laparudi.spawnertools.listeners.world;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SpawnerExplosionListener implements Listener {

    @EventHandler
    public void onSpawnerExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> block.getType() == Material.MOB_SPAWNER);
    }
}
