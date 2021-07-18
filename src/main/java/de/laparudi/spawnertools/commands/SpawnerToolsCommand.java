package de.laparudi.spawnertools.commands;

import de.laparudi.spawnertools.SpawnerTools;
import de.laparudi.spawnertools.util.MySQL;
import de.laparudi.spawnertools.util.UUIDFetcher;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpawnerToolsCommand implements CommandExecutor {
    
    private final List<UUID> cooldown = new ArrayList<>();
    private final MySQL mySQL = SpawnerTools.getPlugin().getMySQL();
    private final String prefix = SpawnerTools.getPlugin().prefix;

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(prefix + "§3SpawnerTools §bv" + SpawnerTools.getPlugin().getDescription().getVersion() + "§3 von §bLapaRudi");
        sender.sendMessage("");
        sender.sendMessage(prefix + "§b/spawnertools list §7[§bSpieler§7]");
        sender.sendMessage(prefix + "§b/spawnertools tp §7<§bWelt§7> <§bX§7> <§bY§7> <§bZ§7>");
        sender.sendMessage("");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 0) {
            sendHelp(sender);
            return true;

        } else if(args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + "§cBenutze /spawnertools list <Spieler>");
                return true;
            }
            Player player = (Player) sender;

            if (args[0].equalsIgnoreCase("list")) {
                UUID uuid = player.getUniqueId();
                List<String> locations = mySQL.getListValue(uuid, "Location");
                List<String> types = mySQL.getListValue(uuid, "Type");
                List<String> spawns = mySQL.getListValue(uuid, "Spawns");

                if (locations.isEmpty()) {
                    player.sendMessage(prefix + "§cDu hast noch keine Spawner platziert.");
                    return true;
                }

                player.sendMessage("");
                player.sendMessage(prefix + "§bSpawnerliste von §3" + player.getName() + "§7 (§9" + locations.size() + "§7)");
                player.sendMessage(prefix + "§7<§bWelt§7> <§bX§7> <§bY§7> <§bZ§7> <§bMob§7> <§bSpawns§7>");
                player.sendMessage("");

                for (int i = 0; i < locations.size(); i++) {
                    String spawner = prefix + "§b" + locations.get(i) + "§7 ▏ §b" + SpawnerTools.getPlugin().getUtil().toTypeString(types.get(i)) + "§7 ▏ §b" + spawns.get(i) + " ";
                    ComponentBuilder builder = new ComponentBuilder(spawner).append("§7[§3TP§7]").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/st tp " +
                            locations.get(i))).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aTeleportiere dich zu diesem Spawner").create()));
                    player.spigot().sendMessage(builder.create());
                }
                player.sendMessage("");
                
            } else if(args[0].equalsIgnoreCase("tp")) {
                sender.sendMessage(prefix + "§b/spawnertools tp §7<§bWelt§7> <§bX§7> <§bY§7> <§bZ§7>");
                
            } else
                sendHelp(sender);
            
        } else if(args.length == 2) {
            if(args[0].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("spawnertools.list.other")) {
                    sender.sendMessage(prefix + "§cDas darfst du nicht.");
                    return true;
                }

                String split = sender instanceof Player ? "▏" : "|";
                
                UUID targetUUID = UUIDFetcher.getUUID(args[1]);
                if(targetUUID == null) {
                    sender.sendMessage(prefix + "§cDieser Spieler wurde nicht gefunden.");
                    return true;
                }
                
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
                if(!target.hasPlayedBefore() && !target.isOnline()) {
                    sender.sendMessage(prefix + "§cDieser Spieler wurde nicht gefunden.");
                    return true;
                }

                List<String> locations = mySQL.getListValue(targetUUID, "Location");
                List<String> types = mySQL.getListValue(targetUUID, "Type");
                List<String> spawns = mySQL.getListValue(targetUUID, "Spawns");

                if (locations == null || locations.isEmpty()) {
                    sender.sendMessage(prefix + "§4" + target.getName() + "§c hat noch keine Spawner platziert.");
                    return true;
                }
                
                sender.sendMessage("");
                sender.sendMessage(prefix + "§bSpawnerliste von §3" + target.getName() + "§7 (§9" + locations.size() + "§7)");
                sender.sendMessage(prefix + "§7<§bWelt§7> <§bX§7> <§bY§7> <§bZ§7> <§bMob§7> <§bSpawns§7>");
                sender.sendMessage("");

                for (int i = 0; i < locations.size(); i++) {
                    String spawner = prefix + "§b" + locations.get(i) + "§7 " + split + " §b" + SpawnerTools.getPlugin().getUtil().toTypeString(types.get(i)) + "§7 " + split + " §b" + spawns.get(i) + " ";
                    ComponentBuilder builder = new ComponentBuilder(spawner).append("§7[§3TP§7]").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/st tp " +
                            locations.get(i))).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aTeleportiere dich zu diesem Spawner").create()));
                    
                    if(sender instanceof Player) {
                        ((Player) sender).spigot().sendMessage(builder.create());
                    } else
                        sender.sendMessage(spawner);
                }
                sender.sendMessage("");
                
            } else if(args[0].equalsIgnoreCase("tp")) {
                sender.sendMessage(prefix + "§b/spawnertools tp §7<§bWelt§7> <§bX§7> <§bY§7> <§bZ§7>");

            } else
                sendHelp(sender);
            
        } else if(args.length == 5) {
            if (!(sender instanceof Player)) {
                return true;
            }
            Player player = (Player) sender;
            
            if(args[0].equalsIgnoreCase("tp")) {
                if(Bukkit.getWorld(args[1]) == null) {
                    player.sendMessage(prefix + "§cDiese Welt existiert nicht.");
                    return true;
                }
                
                if(cooldown.contains(player.getUniqueId())) {
                    player.sendMessage(prefix + "§cBitte warte einen Moment.");
                    return true;
                }
                
                try {
                    Location location = new Location(Bukkit.getWorld(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), player.getLocation().getYaw(), player.getLocation().getPitch());

                    if (!mySQL.spawnerExists(location)) {
                        player.sendMessage(prefix + "§cDieser Spawner existiert nicht mehr.");
                        return true;
                    }
                    if (!mySQL.getValue(location, "UUID").equals(player.getUniqueId().toString()) && !player.hasPermission("spawnertools.bypass.tp")) {
                        player.sendMessage(prefix + "§cDieser Spawner gehört dir nicht.");
                        return true;
                    }
                    
                    cooldown.add(player.getUniqueId());
                    player.teleport(location.add(0.5, 1, 0.5));
                    player.setNoDamageTicks(200);
                    player.sendMessage(prefix + "§bDu wurdest teleportiert.");
                    
                    Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () ->
                            cooldown.remove(player.getUniqueId()), 100);
                    
                } catch (NumberFormatException e) {
                    player.sendMessage(prefix + "§cUngültige Koordinaten.");
                }
            }
        } else
            sendHelp(sender);
        
        return true;
    }
}
