package de.laparudi.spawnertools.listeners.interact;

import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SpawnerUpdateListener implements Listener {
    
    @EventHandler
    public void onSpawnerUpdate(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock().getType() != Material.MOB_SPAWNER) {
            return;
        }
        
        if(event.getItem() == null || event.getItem().getType() != Material.MONSTER_EGG) {
            return;
        }

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        CreatureSpawner spawner = (CreatureSpawner) event.getClickedBlock().getState();
        String mob = spawner.getSpawnedType().name();

        ItemStack spawnEgg = event.getItem().clone();
        spawnEgg.setAmount(1);
        
        if(SpawnerTools.getPlugin().getMySQL().spawnerExists(location) && !player.hasPermission("spawnertools.bypass.update") &&
            !SpawnerTools.getPlugin().getMySQL().getValue(location, "UUID").equals(player.getUniqueId().toString())) {
            
            player.sendMessage(SpawnerTools.getPlugin().prefix + "§cDieser Spawner gehört dir nicht.");
            event.setCancelled(true);
            return;
        }
        
        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> {
                CreatureSpawner newSpawner = (CreatureSpawner) event.getClickedBlock().getState();
                String newMob = newSpawner.getSpawnedType().name();
                
                if(!mob.equals(newMob)) {
                    SpawnerTools.getPlugin().getMySQL().setValue(location, "Type", newSpawner.getSpawnedType().name());
                    player.sendMessage(SpawnerTools.getPlugin().prefix + "§bMob geändert zu: §3" + SpawnerTools.getPlugin().getUtil().toTypeString(newMob));
                
                } else {
                    if(player.getGameMode() != GameMode.CREATIVE) {
                        player.getInventory().addItem(spawnEgg);
                    }
                    player.sendMessage(SpawnerTools.getPlugin().prefix + "§cDieses Mob ist bereits im Spawner.");
                }
        }, 4);
    }
}
