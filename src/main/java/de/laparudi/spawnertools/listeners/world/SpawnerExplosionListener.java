package de.laparudi.spawnertools.listeners.world;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SpawnerExplosionListener implements Listener {

    @EventHandler
    public void onSpawnerExplode(final EntityExplodeEvent event) {
        event.blockList().removeIf(block -> block.getType() == XMaterial.SPAWNER.parseMaterial());
    }
}
