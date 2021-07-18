package de.laparudi.spawnertools.listeners.interact;

import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class OpenSpawnerGUIListener implements Listener {

    @EventHandler
    public void onOpenSpawnerGUI(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock().getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        Material material = player.getItemInHand().getType();
        
        if(material != Material.AIR && material.isBlock() || material == Material.MONSTER_EGG) {
            return;
        }
        
        if(!SpawnerTools.getPlugin().getMySQL().spawnerExists(location)) {
            player.sendMessage(SpawnerTools.getPlugin().prefix + "§9Dieser Spawner hat keinen Besitzer.");
            return;
        }
        
        event.setCancelled(true);
        SpawnerTools.getPlugin().getUtil().openSpawnerGUI(player, location);
    }
}
