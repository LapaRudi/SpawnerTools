package de.laparudi.spawnertools.util;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import de.laparudi.spawnertools.SpawnerTools;
import de.laparudi.spawnertools.util.items.ItemManager;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.UUID;

public class SpawnerUtil implements PluginMessageListener {
    
    private final ItemManager manager = SpawnerTools.getPlugin().getManager();
    
    public ItemStack itemInHand(final Player player) {
        if (Bukkit.getBukkitVersion().contains("1.8.8")) {
            return player.getItemInHand();
        }
        
        return player.getInventory().getItemInMainHand();
    }
    
    public HoverEvent getHoverEvent(final String text) {
        if (SpawnerTools.getPlugin().isOutdated()) {
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create());
        }
        
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(text));
    }
    
    public boolean canHold(final Player player) {
        int space = 0;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (player.getInventory().getItem(i) == null) {
                space++;
            }
        }
        return space >=2;
    }
    
    private Location toPlotLocation(final org.bukkit.Location location) {
        if (location == null || location.getWorld() == null) return null;
        return Location.at(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(),location.getPitch());
    }
    
    public void openSpawnerGUI(final Player player, final Block spawner) {
        final org.bukkit.Location bukkitLocation = spawner.getLocation();
        final MySQL mySQL = SpawnerTools.getPlugin().getMySQL();
        final UUID owner = UUID.fromString(mySQL.getValue(bukkitLocation, "UUID"));
        final String name = Bukkit.getOfflinePlayer(owner).getName();
        final String spawns = NumberFormat.getNumberInstance().format(mySQL.getIntValue(bukkitLocation, "Spawns"));
        final String type = mySQL.getValue(bukkitLocation, "Type");
        
        final Inventory spawnerInventory = Bukkit.createInventory(null, 27, "§0Spawner");
        spawnerInventory.setItem(11, SpawnerTools.getPlugin().getManager().CUSTOM_HEAD(name));
        
        if (player.getUniqueId().compareTo(owner) == 0) {
            spawnerInventory.setItem(13, manager.REMOVE_OK);
        } else
            spawnerInventory.setItem(13, manager.REMOVE_BLOCKED);
        
        spawnerInventory.setItem(15, manager.SPAWNER_STATS(type, spawns, bukkitLocation));

        for (int i = 0; i < spawnerInventory.getSize(); i++) {
            if (spawnerInventory.getItem(i) != null) continue;
            spawnerInventory.setItem(i, manager.GRAY_GLASS);
        }
        
        if (SpawnerTools.getPlugin().getPlotSquared()) {
            final UUID uuid = player.getUniqueId();
            final Location location = this.toPlotLocation(bukkitLocation);
            final Plot plot = Plot.getPlot(location);

            if (plot != null) {
                boolean isTrusted = plot.isOwner(uuid) || plot.getTrusted().contains(uuid) || plot.getMembers().contains(uuid);
                
                // Dem Spieler gehört nicht der Spawner aber er ist Plot-Besitzer
                if (uuid != owner && plot.isOwner(uuid)) {
                        spawnerInventory.setItem(13, manager.REMOVE_PLOT_OWNER);
                        
                // Dem Spieler gehört der Spawner und er hat Trust        
                } else if (uuid == owner && isTrusted) {
                    spawnerInventory.setItem(13, manager.REMOVE_TRUSTED);
                
                // Dem Spieler gehört der Spawner aber er hat kein Trust        
                } else if (uuid == owner) {
                    spawnerInventory.setItem(13, manager.REMOVE_NOTRUST);
                }
            }
        }
        
        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> player.openInventory(spawnerInventory), 2);
    }

    public void sendServerName(final Player player) {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final DataOutputStream stream = new DataOutputStream(out)) {

            stream.writeUTF("GetServer");
            player.sendPluginMessage(SpawnerTools.getPlugin(), "BungeeCord", out.toByteArray());

        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(final String channel, @Nonnull final Player player, @Nonnull final byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        
        final ByteArrayDataInput in = ByteStreams.newDataInput(message);
        final String subchannel = in.readUTF();

        if (subchannel.equals("GetServer")) {
            final String server = in.readUTF();
            SpawnerTools.getPlugin().setServerName("spawnertools_" + server);
            SpawnerTools.getPlugin().getMySQL().setUpdate("CREATE TABLE IF NOT EXISTS `" + SpawnerTools.getPlugin().getServerName() + "` (`Location` VARCHAR(64) UNIQUE, `UUID` CHAR(36), `Type` VARCHAR(32), `Spawns` INT)");
            SpawnerTools.getPlugin().edited = true;
        }
    }
}

