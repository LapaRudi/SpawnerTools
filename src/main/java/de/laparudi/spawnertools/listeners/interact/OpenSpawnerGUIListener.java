package de.laparudi.spawnertools.listeners.interact;

import com.cryptomorin.xseries.XMaterial;
import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class OpenSpawnerGUIListener implements Listener {

    @EventHandler
    public void onOpenSpawnerGUI(final PlayerInteractEvent event) {
        if (!SpawnerTools.getPlugin().isOutdated() && event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final Block block = event.getClickedBlock();
        if (block == null || block.getType() != XMaterial.SPAWNER.parseMaterial()) return;
  
        final Player player = event.getPlayer();
        final Location location = block.getLocation();
        final Material material = SpawnerTools.getPlugin().getUtil().itemInHand(player).getType();
        
        if ( (material != Material.AIR && material.isBlock()) || material.name().contains("_EGG")) {
            return;
        }
        
        if (!SpawnerTools.getPlugin().getMySQL().spawnerExists(location)) {
            player.sendMessage(SpawnerTools.getPlugin().prefix + "§9Dieser Spawner hat keinen Besitzer.");
            return;
        }
        
        event.setCancelled(true);
        SpawnerTools.getPlugin().getUtil().openSpawnerGUI(player, block);
    }
}
