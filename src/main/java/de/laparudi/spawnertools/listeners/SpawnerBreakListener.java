package de.laparudi.spawnertools.listeners;

import de.laparudi.spawnertools.SpawnerTools;
import de.laparudi.spawnertools.util.MySQL;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.UUID;

public class SpawnerBreakListener implements Listener {

    private final String prefix = SpawnerTools.getPlugin().prefix;
    private final MySQL mySQL = SpawnerTools.getPlugin().getMySQL();

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        if (!mySQL.spawnerExists(location)) {
            return;
        }

        if (mySQL.getValue(location, "UUID").equals(player.getUniqueId().toString())) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                player.sendMessage(prefix + "§bDieser Spawner gehört dir. Mit Rechtsklick kannst du den Spawner + das SpawnEi in dein Inventar legen.");
            } else
                mySQL.deleteSpawner(location);
            
        } else if(player.hasPermission("spawnertools.bypass.break") || player.isOp()) {
            mySQL.deleteSpawner(location);
            String type = mySQL.getValue(location, "Type").toLowerCase();
            player.sendMessage(prefix + "§bDu hast einen §3" + StringUtils.capitalize(type) + "§b Spawner von §3" + Bukkit.getOfflinePlayer(UUID.fromString(mySQL.getValue(location, "UUID"))).getName() + " §babgebaut.");
            
        } else {
            event.setCancelled(true);
            player.sendMessage(prefix + "§cDieser Spawner gehöhrt dir nicht.");
        }
    }
}
