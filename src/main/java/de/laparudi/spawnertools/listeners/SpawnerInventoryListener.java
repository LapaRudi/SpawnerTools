package de.laparudi.spawnertools.listeners;

import com.cryptomorin.xseries.XMaterial;
import de.laparudi.spawnertools.SpawnerTools;
import de.laparudi.spawnertools.util.MySQL;
import de.laparudi.spawnertools.util.SpawnerUtil;
import de.laparudi.spawnertools.util.items.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpawnerInventoryListener implements Listener {

    private final SpawnerUtil util = SpawnerTools.getPlugin().getUtil();
    private final MySQL mySQL = SpawnerTools.getPlugin().getMySQL();
    private final String prefix = SpawnerTools.getPlugin().prefix;

    @EventHandler
    public void onSpawnerGUIClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        if (inventory == null || !event.getView().getTitle().equals("§0Spawner")) return;

        event.setCancelled(true);
        final ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() != Material.EMERALD || !new ItemBuilder(item).hasFlag()) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();
        final ItemStack stats = inventory.getItem(15);
        if (stats == null) return;
        final ItemMeta meta = stats.getItemMeta();

        if (meta == null || meta.getLore() == null) return;
        final String lore = meta.getLore().get(3);
        final String locationString = lore.substring(lore.lastIndexOf(":") + 4);
        final Location location = SpawnerTools.getPlugin().getManager().fromMySQLString(locationString);
        player.closeInventory();

        if (!mySQL.spawnerExists(location)) {
            player.sendMessage(prefix + "§cDieser Spawner existiert nicht mehr.");
            return;
        }
        
        if (!util.canHold(player)) {
            player.sendMessage(prefix + "§cDu brauchst 2 freie Slots im Inventar um den Spawner abzubauen.");
            return;
        }
        
        final EntityType type = EntityType.valueOf(mySQL.getValue(location, "Type"));
        
        if (type != EntityType.PIG) {
            try {
                player.getInventory().addItem(XMaterial.valueOf(type.name() + "_SPAWN_EGG").parseItem());
                
            } catch (final IllegalArgumentException exception) {
                player.sendMessage(SpawnerTools.getPlugin().getPrefix() + "§cDieses Mob gibt es nicht als Spawn-Ei. §7(§b" + type + "§7) " +
                        "§cMelde dich bei einem Teammitglied oder ändere das Mob im Spawner.");
                return;
            }
        }
        
        location.getBlock().setType(Material.AIR);
        mySQL.deleteSpawner(location);
        player.getInventory().addItem(XMaterial.SPAWNER.parseItem());
        player.sendMessage(prefix + "§bDer Spawner wurde abgebaut. Du hast die Items erhalten.");
    }
}
