package de.laparudi.spawnertools.listeners;

import de.laparudi.spawnertools.SpawnerTools;
import de.laparudi.spawnertools.util.ItemBuilder;
import de.laparudi.spawnertools.util.MySQL;
import de.laparudi.spawnertools.util.SpawnerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class SpawnerInventoryListener implements Listener {

    private final SpawnerUtil util = SpawnerTools.getPlugin().getUtil();
    private final MySQL mySQL = SpawnerTools.getPlugin().getMySQL();
    private final String prefix = SpawnerTools.getPlugin().prefix;

    @EventHandler
    public void onSpawnerGUIClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !inventory.getTitle().equals("§0Spawner")) {
            return;
        }
        
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item.getType() != Material.EMERALD || !item.getItemMeta().hasItemFlag(ItemFlag.HIDE_POTION_EFFECTS)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String lore = inventory.getItem(15).getItemMeta().getLore().get(3);
        String locationString = lore.substring(lore.lastIndexOf(":") + 4);
        Location location = util.fromMySQLString(locationString);
        player.closeInventory();
        
        if (mySQL.spawnerExists(location)) {
            if (util.canHold(player)) {
                
                location.getBlock().setType(Material.AIR);
                EntityType type = EntityType.valueOf(mySQL.getValue(location, "Type"));
                int id = type.getTypeId();
                mySQL.deleteSpawner(location);
                
                player.getInventory().addItem(new ItemStack(Material.MOB_SPAWNER));
                if(id != 90) {  //Schweine SpawnEggs droppen nicht
                    player.getInventory().addItem(new ItemBuilder(Material.MONSTER_EGG).setDurability(id).toItemStack());
                }
                player.sendMessage(prefix + "§bDer Spawner wurde abgebaut. Du hast die Items erhalten.");

            } else
                player.sendMessage(prefix + "§cDu brauchst 2 freie Slots im Inventar um den Spawner abzubauen.");
        } else
            player.sendMessage(prefix + "§cDieser Spawner existiert nicht mehr.");
    }
}
