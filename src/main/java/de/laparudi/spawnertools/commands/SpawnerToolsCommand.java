package de.laparudi.spawnertools.commands;

import de.laparudi.spawnertools.SpawnerTools;
import de.laparudi.spawnertools.util.MySQL;
import de.laparudi.spawnertools.util.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SpawnerToolsCommand implements CommandExecutor, TabCompleter {

    private final List<UUID> cooldown = new ArrayList<>();
    private final MySQL mySQL = SpawnerTools.getPlugin().getMySQL();
    private final String prefix = SpawnerTools.getPlugin().getPrefix();
    private final ChatColor color = SpawnerTools.getPlugin().getColor();

    private void sendHelp(final CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(prefix + "§3SpawnerTools " + color + "v" + SpawnerTools.getPlugin().getDescription().getVersion() + "§3 von " + color + "LapaRudi");
        sender.sendMessage("");
        sender.sendMessage(prefix + color + "/spawnertools list §7[" + color + "Spieler§7]");
        sender.sendMessage(prefix + color + "/spawnertools tp §7<" + color + "Welt§7> <" + color + "X§7> <" + color + "Y§7> <" + color + "Z§7>");
        sender.sendMessage("");
    }
    
    private void showList(final CommandSender sender, final UUID uuid) {
        final String split = sender instanceof Player ? "▏" : "|";
        final List<String> locations = mySQL.getListValue(uuid, "Location");
        final List<String> types = mySQL.getListValue(uuid, "Type");
        final List<String> spawns = mySQL.getListValue(uuid, "Spawns");

        if (locations.isEmpty()) {
            final String message = sender.getName().equals(UUIDFetcher.getName(uuid)) ?
                    "§cDu hast noch keine Spawner platziert." : "§cDieser Spieler hat noch keine Spawner platziert.";
            
            sender.sendMessage(prefix + message);
            return;
        }

        sender.sendMessage("");
        sender.sendMessage(prefix + color + "Spawnerliste von §c" + UUIDFetcher.getName(uuid) + "§7 (§3" + locations.size() + "§7)");
        sender.sendMessage(prefix + "§7<" + color + "Welt§7> <" + color + "X§7> <" + color + "Y§7> <" + color + "Z§7> <" + color + "Mob§7> <" + color + "Spawns§7>");
        sender.sendMessage("");

        for (int i = 0; i < locations.size(); i++) {
            final String spawner = prefix + color + locations.get(i) + "§7 " + split + " " + color +
                    SpawnerTools.getPlugin().getManager().toMobString(types.get(i)) + "§7 " + split + " " + color + spawns.get(i) + " ";

            final ComponentBuilder builder = SpawnerTools.getPlugin().isOutdated() ? new ComponentBuilder(prefix) :
                    new ComponentBuilder("[").color(ChatColor.of("#3e5556"))
                            .append("S").color(ChatColor.of("#02a0a6")).append("p").color(ChatColor.of("#00abb7")).append("a").color(ChatColor.of("#00b6c8"))
                            .append("w").color(ChatColor.of("#00c2da")).append("n").color(ChatColor.of("#00cdec")).append("er").color(ChatColor.of("#00d8ff"))
                            .append("T").color(ChatColor.of("#00cdec")).append("o").color(ChatColor.of("#00c2da")).append("o").color(ChatColor.of("#00b6c8"))
                            .append("l").color(ChatColor.of("#00abb7")).append("s").color(ChatColor.of("#02a0a6")).append("] ").color(ChatColor.of("#3e5556"));

            builder.append(locations.get(i)).color(color)
                    .append(" ").append(split).color(ChatColor.GRAY).append(" ")
                    .append(SpawnerTools.getPlugin().getManager().toMobString(types.get(i))).color(color)
                    .append(" ").append(split).color(ChatColor.GRAY).append(" ")
                    .append(spawns.get(i)).color(color).append("§7 [§3TP§7]")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spawnertools tp " + locations.get(i)))
                    .event(SpawnerTools.getPlugin().getUtil().getHoverEvent("§aTeleportiere dich zu diesem Spawner"));

            if (sender instanceof Player) {
                ((Player) sender).spigot().sendMessage(builder.create());
            } else
                sender.sendMessage(spawner);
        }

        sender.sendMessage("");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        switch (args.length) {
            case 0:
                this.sendHelp(sender);
                return true;

            case 1:
                if (!(sender instanceof Player)) {
                    sender.sendMessage(prefix + "§cBenutze /spawnertools list <Spieler>");
                    return true;
                }

                Player player = (Player) sender;

                if (args[0].equalsIgnoreCase("list")) {
                    this.showList(sender, player.getUniqueId());

                } else if (args[0].equalsIgnoreCase("tp")) {
                    sender.sendMessage(prefix + color + "/spawnertools tp §7<" + color + "Welt§7> <" + color + "X§7> <" + color + "Y§7> <" + color + "Z§7>");

                } else
                    this.sendHelp(sender);
                
                break;

            case 2:
                if (args[0].equalsIgnoreCase("list")) {
                    if (!sender.hasPermission("spawnertools.list.other")) {
                        sender.sendMessage(prefix + "§cDu darfst nur deine eigene Liste ansehen.");
                        return true;
                    }
                    
                    final UUID targetUUID = UUIDFetcher.getUUID(args[1]);

                    if (targetUUID == null) {
                        sender.sendMessage(prefix + "§cDieser Spieler wurde nicht gefunden.");
                        return true;
                    }
                    
                    this.showList(sender, targetUUID);

                } else if (args[0].equalsIgnoreCase("tp")) {
                    sender.sendMessage(prefix + "§b/spawnertools tp §7<§bWelt§7> <§bX§7> <§bY§7> <§bZ§7>");

                } else
                    this.sendHelp(sender);
                
                break;

            case 5:
                if (!(sender instanceof Player)) {
                    return true;
                }
                
                player = (Player) sender;

                if (args[0].equalsIgnoreCase("tp")) {
                    if (Bukkit.getWorld(args[1]) == null) {
                        player.sendMessage(prefix + "§cDiese Welt existiert nicht.");
                        return true;
                    }

                    if (cooldown.contains(player.getUniqueId())) {
                        player.sendMessage(prefix + "§cBitte warte einen Moment.");
                        return true;
                    }

                    try {
                        final Location location = new Location(Bukkit.getWorld(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), player.getLocation().getYaw(), player.getLocation().getPitch());

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
                        player.setNoDamageTicks(100);
                        player.sendMessage(prefix + "§bDu wurdest teleportiert.");
                        Bukkit.getScheduler().runTaskLater(SpawnerTools.getPlugin(), () -> cooldown.remove(player.getUniqueId()), 100);

                    } catch (final NumberFormatException exception) {
                        player.sendMessage(prefix + "§cUngültige Koordinaten.");
                    }
                }
                break;
                
            default:
                this.sendHelp(sender);
        }

        return true;
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        final List<String> completion = new ArrayList<>();
        
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("list", "tp"), completion);
            return completion;

        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list") && sender.hasPermission("spawnertools.list.other")) {
                return null;
            }
        }
        
        return Collections.emptyList();
    }
}
