package de.laparudi.spawnertools.listeners;

import de.laparudi.spawnertools.SpawnerTools;
import de.laparudi.spawnertools.util.SpawnerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final SpawnerUtil util = SpawnerTools.getPlugin().getUtil();
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(!SpawnerTools.getPlugin().getServerName().equals("Server")) {
            return;
        }
        
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> 
                util.sendServerName(player), 10L);
        
        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> {
            if(SpawnerTools.getPlugin().getServerName().equals("Server")) {
                SpawnerTools.getPlugin().getMySQL().setUpdate("CREATE TABLE IF NOT EXISTS `SpawnerTools` (`Location` VARCHAR(64), `UUID` CHAR(36), `Type` VARCHAR(32), `Spawns` INT)");
            }
        }, 40L);
    }
}
