package de.laparudi.spawnertools.listeners;

import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        if(SpawnerTools.getPlugin().edited) return;
        
        final Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () ->
                SpawnerTools.getPlugin().getUtil().sendServerName(player), 10L);
        
        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> {
            if (SpawnerTools.getPlugin().getServerName().equals("spawnertools")) {
                SpawnerTools.getPlugin().getMySQL().setUpdate("CREATE TABLE IF NOT EXISTS `spawnertools` (`Location` VARCHAR(64) UNIQUE, `UUID` CHAR(36), `Type` VARCHAR(32), `Spawns` INT)");
            }
        }, 40L);
    }
}
