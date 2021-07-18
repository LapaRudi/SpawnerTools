package de.laparudi.spawnertools;

import de.laparudi.spawnertools.commands.SpawnerToolsCommand;
import de.laparudi.spawnertools.listeners.*;
import de.laparudi.spawnertools.listeners.interact.OpenSpawnerGUIListener;
import de.laparudi.spawnertools.listeners.interact.SpawnerUpdateListener;
import de.laparudi.spawnertools.listeners.JoinListener;
import de.laparudi.spawnertools.listeners.SpawnerBreakListener;
import de.laparudi.spawnertools.listeners.SpawnerCommandListener;
import de.laparudi.spawnertools.listeners.SpawnerPlaceListener;
import de.laparudi.spawnertools.listeners.world.SpawnerCountListener;
import de.laparudi.spawnertools.listeners.world.SpawnerExplosionListener;
import de.laparudi.spawnertools.util.MySQL;
import de.laparudi.spawnertools.util.SpawnerUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

import static org.bukkit.ChatColor.*;

public final class SpawnerTools extends JavaPlugin {

    private MySQL mySQL;
    private static SpawnerTools plugin;
    private boolean plotSquared = false;
    private final SpawnerUtil util = new SpawnerUtil();
    private final File configFile = new File(this.getDataFolder(), "config.yml");
    
    public final String prefix = DARK_GRAY + "[" + DARK_AQUA + "SpawnerTools" + DARK_GRAY + "] ";
    public String serverName = "Server";
    
    @Override
    public void onEnable() {
        plugin = this;
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", util);

        this.loadConfig();
        this.connectMySQL();
        this.setPlotSquared();
        this.loadListeners();
        this.getCommand("spawnertools").setExecutor(new SpawnerToolsCommand());
        Bukkit.getConsoleSender().sendMessage(prefix + DARK_GREEN + "SpawnerTools " + RED + "v" + this.getDescription().getVersion() + DARK_GREEN + " geladen.");
    }

    @Override
    public void onDisable() {
        if(mySQL != null) {
            mySQL.disconnect();
        }
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        Bukkit.getConsoleSender().sendMessage(prefix + DARK_RED + "SpawnerTools " + RED + "v" + this.getDescription().getVersion() + DARK_RED + " deaktiviert.");
    }
    
    private void loadListeners() {
        PluginManager manager = Bukkit.getPluginManager();
        Listener[] listeners = new Listener[] {
                new JoinListener(), new OpenSpawnerGUIListener(), new SpawnerBreakListener(),
                new SpawnerCountListener(), new SpawnerInventoryListener(), new SpawnerPlaceListener(),
                new SpawnerExplosionListener(), new SpawnerCommandListener(), new SpawnerUpdateListener()
        };
        Arrays.stream(listeners).forEach(listener -> manager.registerEvents(listener, this));
    }
    
    private void loadConfig() {
        if(!configFile.exists()) {
            this.saveResource("config.yml", false);
        }
    }
    
    private void connectMySQL() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if(config.getString("mysql.database") == null || config.getString("mysql.database").equals("database")) {
            Bukkit.getConsoleSender().sendMessage(prefix + "§cDu musst eine Datenbank in der Config festlegen.");
            return;
        }
        mySQL = new MySQL(config.getString("mysql.host"), config.getInt("mysql.port"), config.getString("mysql.username"), config.getString("mysql.password"), config.getString("mysql.database"));
        mySQL.create();
        mySQL.connect();
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
    
    public MySQL getMySQL() {
        return mySQL;
    }

    public SpawnerUtil getUtil() {
        return util;
    }
    
    public static SpawnerTools getPlugin() {
        return plugin;
    }
}
