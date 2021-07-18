package de.laparudi.spawnertools.util;

import de.laparudi.spawnertools.SpawnerTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public class MySQL {

    private final String host, username, password, database;
    private final int port;
    private Connection connection;
    
    private final SpawnerUtil util = SpawnerTools.getPlugin().getUtil();
    private final String prefix = SpawnerTools.getPlugin().prefix;

    public MySQL(String host, int port, String username, String password, String database) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
        this.database = database;
    }
    
    public void create() {
        try {
            setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port, username, password));
            setUpdate("CREATE DATABASE IF NOT EXISTS `" + database + "`");
            disconnect();
            
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + RED + "MySQL Verbindung konnte nicht hergestellt werden. Überprüfe die Config.");
        }
    }
    
    public void connect() {
        try {
            if (isConnected() && !getConnection().isClosed()) {
                Bukkit.getConsoleSender().sendMessage(prefix + YELLOW + "Eine MySQL Verbindung wurde bereits aufgebaut.");
                return;
            }
            setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password));
            Bukkit.getConsoleSender().sendMessage(prefix + GREEN + "MySQL Verbindung wurde aufgebaut.");

        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + RED + "MySQL Verbindung konnte nicht hergestellt werden. Überprüfe die Config.");
        }
    }

    public void disconnect() {
        if (!isConnected()) {
            return;
        }
        
        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setUpdate(String value) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(value)) {
            statement.executeUpdate();

        } catch ( Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean spawnerExists(Location location) {
        try (PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                .prepareStatement("SELECT * FROM `SpawnerTools_" + SpawnerTools.getPlugin().getServerName() + "` WHERE `Location` = '" + util.toMySQLString(location) + "'");

             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getString("Location") != null;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(SpawnerTools.getPlugin().prefix + "§cMySQL-Fehler | Das Plugin sollte nicht reloaded werden. Starte den ganzen Server neu.");
        }
        return false;
    }

    public void createSpawner(Location location, UUID uuid, String type) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(SpawnerTools.getPlugin(), () -> {
            if (!spawnerExists(location)) {

                try (PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                        .prepareStatement("INSERT INTO `SpawnerTools_" + SpawnerTools.getPlugin().getServerName() + "` (Location, UUID, Type, Spawns) VALUES (?, ?, ?, ?)")) {

                    statement.setString(1, util.toMySQLString(location));
                    statement.setString(2, uuid.toString());
                    statement.setString(3, type);
                    statement.setInt(4, 0);
                    statement.executeUpdate();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void deleteSpawner(Location location) {
        Bukkit.getScheduler().runTaskAsynchronously(SpawnerTools.getPlugin(), () -> {
           if(spawnerExists(location)) {
               try(PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().connection
               .prepareStatement("DELETE FROM `SpawnerTools_" + SpawnerTools.getPlugin().getServerName() + "` WHERE `Location` = '" + util.toMySQLString(location) + "'")) {
                   
                   statement.executeUpdate();
                   
               } catch (SQLException e) {
                   e.printStackTrace();
               }
           }
        });
    }
    
    public void setValue(Location location, String type, String value ) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(SpawnerTools.getPlugin(), () -> {
            if (spawnerExists(location)) {

                try (PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                        .prepareStatement("UPDATE `SpawnerTools_" + SpawnerTools.getPlugin().getServerName() + "` SET `" + type + "` = '" + value + "' WHERE `Location` = '" + util.toMySQLString(location) + "'")) {

                    statement.executeUpdate();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    
    public String getValue(Location location, String type ) {
        if (spawnerExists(location)) {

            try (PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                    .prepareStatement("SELECT * FROM `SpawnerTools_" + SpawnerTools.getPlugin().getServerName() + "` WHERE `Location` = '" + util.toMySQLString(location) + "'");
                
                ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString(type);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public List<String> getListValue(UUID uuid, String type ) {
        List<String> list = new ArrayList<>();
            try (PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                    .prepareStatement("SELECT * FROM `SpawnerTools_" + SpawnerTools.getPlugin().getServerName() + "` WHERE `UUID` = '" + uuid.toString() + "'");

                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(resultSet.getString(type));
                }
                return list;
                
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        return null;
    }
    
    public int getIntValue(Location location, String type) {
        if (spawnerExists(location)) {

            try (PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                    .prepareStatement("SELECT * FROM `SpawnerTools_" + SpawnerTools.getPlugin().getServerName() + "` WHERE `Location` = '" + util.toMySQLString(location) + "'");

                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(type);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public void setIntValue(Location location, String type, int value) {
        Bukkit.getScheduler().runTaskAsynchronously(SpawnerTools.getPlugin(), () -> {
            if (spawnerExists(location)) {

                try (PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                        .prepareStatement("UPDATE `SpawnerTools_" + SpawnerTools.getPlugin().getServerName() + "` SET `" + type + "` = " + value + " WHERE `Location` = '" + util.toMySQLString(location) + "'")) {

                    statement.executeUpdate();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void checkConnection() {
        try {
            if (!isConnected() || !this.connection.isValid(10) || this.connection.isClosed()) connect();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection( Connection connection ) {
        this.connection = connection;
    }
    
    
}
