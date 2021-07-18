package de.laparudi.spawnertools.util;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.intellectualcrafters.plot.api.PlotAPI;
import de.laparudi.spawnertools.SpawnerTools;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.UUID;

public class SpawnerUtil implements PluginMessageListener {
    
    public String toMySQLString(Location location) {
        return location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }
 
    public Location fromMySQLString(String string) {
        String[] values = string.split(" ");
        return new Location(Bukkit.getWorld(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3]));
    }
    
    public String toTypeString(String type) {
        return WordUtils.capitalize(type.toLowerCase().replace('_', ' '));
    }
    
    public boolean canHold(Player player) {
        int space = 0;
        for(int i = 0; i < player.getInventory().getSize(); i++) {
            if(player.getInventory().getItem(i) == null) {
                space++;
            }
        }
        return space >=2;
    }
    
    public void openSpawnerGUI(Player player, Location location) {
        MySQL mySQL = SpawnerTools.getPlugin().getMySQL();
        UUID uuid = UUID.fromString(mySQL.getValue(location, "UUID"));
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        String spawns = NumberFormat.getNumberInstance().format(mySQL.getIntValue(location, "Spawns"));
        String type = mySQL.getValue(location, "Type");
        
        Inventory spawnerInventory = Bukkit.createInventory(null, 27, "§0Spawner");
        spawnerInventory.setItem(11, new ItemBuilder(Material.SKULL_ITEM).setDurability(3).setName("§3Besitzer").setLore("", "§7» §bDieser Spawner gehört §3" + name + "§b.", "").setSkullOwner(name).toItemStack());

        if (player.getUniqueId().equals(uuid)) {
            spawnerInventory.setItem(13, new ItemBuilder(Material.EMERALD).setName("§3Spawner abbauen").setLore("", "§7» §bWenn du deinen Spawner abbaust, bekommst du", "§7» §bden Spawner und das SpawnEi in dein Inventar.", "").addFlag().toItemStack());
        } else
            spawnerInventory.setItem(13, new ItemBuilder(Material.REDSTONE).setName("§3Spawner abbauen").setLore("", "§7» §cNur der Besitzer des Spawners kann ihn abbauen.", "").toItemStack());

        spawnerInventory.setItem(15, new ItemBuilder(Material.BOOK).setName("§3Statisiken").setLore("", "§7» §3Mob: §b" + toTypeString(type), "§7» §3Spawns: §b" + spawns, "§7» §3Location: §b" + toMySQLString(location), "").toItemStack());

        for (int i = 0; i < spawnerInventory.getSize(); i++) {
            if (spawnerInventory.getItem(i) == null) {
                spawnerInventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability(7).setName(" ").toItemStack());
            }
        }
        
        if(SpawnerTools.getPlugin().getPlotSquared()) {
            PlotAPI api = new PlotAPI();
            if (api.getPlot(location) != null) {
                boolean isTrusted = api.getPlot(location).isOwner(player.getUniqueId()) || api.getPlot(location).getTrusted().contains(player.getUniqueId()) || api.getPlot(location).getMembers().contains(player.getUniqueId());
                
                // Dem Spieler gehört nicht der Spawner aber er ist Plot-Besitzer
                if (!player.getUniqueId().equals(uuid) && api.getPlot(location).isOwner(player.getUniqueId())) {
                        spawnerInventory.setItem(13, new ItemBuilder(Material.EMERALD).setName("§3Spawner abbauen").setLore("", "§7» §bWenn du deinen Spawner abbaust, bekommst du", "§7» §bden Spawner und das SpawnEi in dein Inventar.", "", "§7» §9Weil du der Plot-Besitzer bist,", "§7» §9kannst du den Spawner abbauen.", "").addFlag().toItemStack());
                        
                // Dem Spieler gehört der Spawner und er hat Trust        
                } else if(player.getUniqueId().equals(uuid) && isTrusted) {
                    spawnerInventory.setItem(13, new ItemBuilder(Material.EMERALD).setName("§3Spawner abbauen").setLore("", "§7» §bWenn du deinen Spawner abbaust, bekommst du", "§7» §bden Spawner und das SpawnEi in dein Inventar.", "", "§7» §9Weil dieser Spawner dir gehört, und du auf diesem", "§7» §9Grundstück vertraut bist kannst du den Spawner abbauen.", "").addFlag().toItemStack());
                
                // Dem Spieler gehört der Spawner aber er hat kein Trust        
                } else if(player.getUniqueId().equals(uuid) && !isTrusted) {
                    spawnerInventory.setItem(13, new ItemBuilder(Material.REDSTONE).setName("§3Spawner abbauen").setLore("", "§7» §cAuch wenn dieser Spawner dir gehört,", "§7» §cdarfst du ihn nur abbauen, wenn du", "§7» §cauf diesem Grundstück vertraut bist.", "").toItemStack());
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> 
                player.openInventory(spawnerInventory), 2);
    }

    public void sendServerName(Player player) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream stream = new DataOutputStream(out)) {

            stream.writeUTF("GetServer");
            player.sendPluginMessage(SpawnerTools.getPlugin(), "BungeeCord", out.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("GetServer")) {
            String server = in.readUTF();
            SpawnerTools.getPlugin().setServerName(server);
            SpawnerTools.getPlugin().getMySQL().setUpdate("CREATE TABLE IF NOT EXISTS `SpawnerTools_" + server + "` (`Location` VARCHAR(64) UNIQUE, `UUID` CHAR(36), `Type` VARCHAR(32), `Spawns` INT)");
        }
    }
}

