package de.laparudi.spawnertools;

import de.laparudi.spawnertools.commands.SpawnerToolsCommand;
import de.laparudi.spawnertools.listeners.*;
import de.laparudi.spawnertools.listeners.interact.OpenSpawnerGUIListener;
import de.laparudi.spawnertools.listeners.interact.SpawnerUpdateListener;
import de.laparudi.spawnertools.listeners.world.SpawnerCountListener;
import de.laparudi.spawnertools.listeners.world.SpawnerExplosionListener;
import de.laparudi.spawnertools.util.MySQL;
import de.laparudi.spawnertools.util.SpawnerUtil;
import de.laparudi.spawnertools.util.items.ItemManager;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static org.bukkit.ChatColor.*;

@Getter
public final class SpawnerTools extends JavaPlugin {

    private MySQL mySQL;
    private static @Getter SpawnerTools plugin;
    private ItemManager manager;
    private boolean plotSquared = false;
    private SpawnerUtil util;
    private final File configFile = new File(this.getDataFolder(), "config.yml");
    
    public String prefix = DARK_GRAY + "[" + DARK_AQUA + "SpawnerTools" + DARK_GRAY + "] ";
    public ChatColor color = ChatColor.AQUA;
    public boolean edited = false;
    public boolean outdated = false;
    public String serverName = "spawnertools";
    
    @Override
    public void onEnable() {
        plugin = this;

        this.initialize();
        this.loadConfig();
        this.connectMySQL();
        if (mySQL == null) return;
        this.setPlotSquared();
        this.loadListeners();
        
        Objects.requireNonNull(this.getCommand("spawnertools")).setExecutor(new SpawnerToolsCommand());
        Bukkit.getScheduler().runTaskLater(this, this::loadServerName, 20);
        Bukkit.getConsoleSender().sendMessage(prefix + DARK_GREEN + "SpawnerTools " + RED + "v" + this.getDescription().getVersion() + DARK_GREEN + " geladen.");
    }

    @Override
    public void onDisable() {
        if (mySQL != null) {
            mySQL.disconnect();
        }
        
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        Bukkit.getConsoleSender().sendMessage(prefix + DARK_RED + "SpawnerTools " + RED + "v" + this.getDescription().getVersion() + DARK_RED + " deaktiviert.");
    }
    
    private void initialize() {
        this.setPrefix();
        this.manager = new ItemManager();
        this.util = new SpawnerUtil();

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", util);
    }
    
    private void loadServerName() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        final Player player = Bukkit.getOnlinePlayers().stream().findAny().get();
        util.sendServerName(player);
    }
    
    private void loadListeners() {
        final Listener[] listeners = new Listener[] {
                new JoinListener(), new OpenSpawnerGUIListener(), new SpawnerBreakListener(),
                new SpawnerCountListener(), new SpawnerInventoryListener(), new SpawnerPlaceListener(),
                new SpawnerExplosionListener(), new SpawnerCommandListener(), new SpawnerUpdateListener() };
        
        Arrays.stream(listeners).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }
    
    private void loadConfig() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", false);
        }
    }
    
    private void connectMySQL() {
        final FileConfiguration config = this.getConfig();
        if (config.getString("mysql.database") == null || config.getString("mysql.database").equals("database")) {
            Bukkit.getConsoleSender().sendMessage(prefix + "§cDu musst eine Datenbank in der Config festlegen.");
            return;
        }
        
        mySQL = new MySQL(config.getString("mysql.host"), config.getInt("mysql.port"), config.getString("mysql.username"), config.getString("mysql.password"), config.getString("mysql.database"));
        mySQL.create();
        mySQL.connect();
    }
    
    private void setPrefix() {
        final String version = Bukkit.getBukkitVersion();
        final String[] hexColorSupport = new String[] { "1.16", "1.17", "1.18" };
        
        if (Arrays.stream(hexColorSupport).anyMatch(version::contains)) {
            this.color = ChatColor.of("#00bcff");
            this.prefix = ChatColor.of("#3e5556") + "[" + ChatColor.of("#02a0a6") + "S" + ChatColor.of("#00abb7") + "p" +
                    ChatColor.of("#00b6c8") + "a" + ChatColor.of("#00c2da") + "w" + ChatColor.of("#00cdec") + "n" +
                    ChatColor.of("#00d8ff") + "er" + ChatColor.of("#00cdec") + "T" + ChatColor.of("#00c2da") + "o" +
                    ChatColor.of("#00b6c8") + "o" + ChatColor.of("#00abb7") + "l" + ChatColor.of("#02a0a6") + "s" +
                    ChatColor.of("#3e5556") + "] " + SpawnerTools.getPlugin().getColor();
            
        } else
            this.outdated = true;
    }
    
    private void setPlotSquared() {
        plotSquared = Bukkit.getPluginManager().getPlugin("PlotSquared") != null;
    }

    public boolean getPlotSquared() {
        return plotSquared;
    }
    
    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
