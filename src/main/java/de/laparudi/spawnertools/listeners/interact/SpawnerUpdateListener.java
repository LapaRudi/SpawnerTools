package de.laparudi.spawnertools.listeners.interact;

import com.cryptomorin.xseries.XMaterial;
import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SpawnerUpdateListener implements Listener {
    
    @EventHandler
    public void onSpawnerUpdate(final PlayerInteractEvent event) {
        if (!SpawnerTools.getPlugin().isOutdated() && event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final ItemStack item = event.getItem();
        final Block block = event.getClickedBlock();
        
        if (item == null || !item.getType().name().contains("_EGG")) return;
        if (block == null || block.getType() != XMaterial.SPAWNER.parseMaterial()) return;

        final Player player = event.getPlayer();
        final Location location = block.getLocation();
        final CreatureSpawner spawner = (CreatureSpawner) block.getState();
        final String mob = spawner.getSpawnedType().name();

        final ItemStack spawnEgg = item.clone();
        spawnEgg.setAmount(1);
        
        if (SpawnerTools.getPlugin().getMySQL().spawnerExists(location) && !player.hasPermission("spawnertools.bypass.update") &&
            !SpawnerTools.getPlugin().getMySQL().getValue(location, "UUID").equals(player.getUniqueId().toString())) {
            
            player.sendMessage(SpawnerTools.getPlugin().prefix + "§cDieser Spawner gehört dir nicht.");
            event.setCancelled(true);
            return;
        }
        
        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> {
                final CreatureSpawner newSpawner = (CreatureSpawner) block.getState(); // TODO event.getCLBlock ??
                final String newMob = newSpawner.getSpawnedType().name();
                
                if (!mob.equals(newMob)) {
                    SpawnerTools.getPlugin().getMySQL().setValue(location, "Type", newSpawner.getSpawnedType().name());
                    player.sendMessage(SpawnerTools.getPlugin().prefix + "§bMob geändert zu: §3" + SpawnerTools.getPlugin().getManager().toMobString(newMob));
                
                } else {
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        player.getInventory().addItem(spawnEgg);
                    }
                    player.sendMessage(SpawnerTools.getPlugin().prefix + "§cDieses Mob ist bereits im Spawner.");
                }
        }, 4);
    }
}
