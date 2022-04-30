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
    private final String prefix = SpawnerTools.getPlugin().prefix;

    public MySQL(final String host, final int port, final String username, final String password, final String database) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
        this.database = database;
    }
    
    public void create() {
        try {
            this.setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port, username, password));
            this.setUpdate("CREATE DATABASE IF NOT EXISTS `" + database + "`");
            this.disconnect();
            
        } catch (final SQLException exception) {
            Bukkit.getConsoleSender().sendMessage(prefix + RED + "MySQL Verbindung konnte nicht hergestellt werden. Überprüfe die Config.");
        }
    }
    
    public void connect() {
        try {
            if (this.isConnected() && !this.getConnection().isClosed()) {
                Bukkit.getConsoleSender().sendMessage(prefix + YELLOW + "Eine MySQL Verbindung wurde bereits aufgebaut.");
                return;
            }
            
            this.setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password));
            Bukkit.getConsoleSender().sendMessage(prefix + GREEN + "MySQL Verbindung wurde aufgebaut.");

        } catch (final SQLException exception) {
            Bukkit.getConsoleSender().sendMessage(prefix + RED + "MySQL Verbindung konnte nicht hergestellt werden. Überprüfe die Config.");
        }
    }

    public void disconnect() {
        if (!this.isConnected()) {
            return;
        }
        
        try {
            connection.close();
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setUpdate(final String value) {
        this.checkConnection();
        
        try (final PreparedStatement statement = connection.prepareStatement(value)) {
            statement.executeUpdate();

        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }
    
    public boolean spawnerExists(final Location location) {
        try (final PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                .prepareStatement("SELECT * FROM `" + SpawnerTools.getPlugin().getServerName() + "` WHERE `Location` = '" + SpawnerTools.getPlugin().getManager().toMySQLString(location) + "'");

             final ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getString("Location") != null;
            }

        } catch (final SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(SpawnerTools.getPlugin().prefix + "§cMySQL-Fehler | Das Plugin sollte nicht reloaded werden. Starte den ganzen Server neu.");
        }
        return false;
    }

    public void createSpawner(final Location location, final UUID uuid, final String type) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(SpawnerTools.getPlugin(), () -> {
            if (this.spawnerExists(location)) return;

            try (final PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                    .prepareStatement("INSERT INTO `" + SpawnerTools.getPlugin().getServerName() + "` (Location, UUID, Type, Spawns) VALUES (?, ?, ?, ?)")) {

                statement.setString(1, SpawnerTools.getPlugin().getManager().toMySQLString(location));
                statement.setString(2, uuid.toString());
                statement.setString(3, type);
                statement.setInt(4, 0);
                statement.executeUpdate();

            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void deleteSpawner(final Location location) {
        Bukkit.getScheduler().runTaskAsynchronously(SpawnerTools.getPlugin(), () -> {
            if (!this.spawnerExists(location)) return;

            try (final PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                    .prepareStatement("DELETE FROM `" + SpawnerTools.getPlugin().getServerName() + "` WHERE `Location` = '" + SpawnerTools.getPlugin().getManager().toMySQLString(location) + "'")) {

                statement.executeUpdate();

            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }
    
    public void setValue(final Location location, final String type, final String value ) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(SpawnerTools.getPlugin(), () -> {
            if (!this.spawnerExists(location)) return;

            try (final PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                    .prepareStatement("UPDATE `" + SpawnerTools.getPlugin().getServerName() + "` SET `" + type + "` = '" + value + "' WHERE `Location` = '" + SpawnerTools.getPlugin().getManager().toMySQLString(location) + "'")) {

                statement.executeUpdate();

            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }
    
    public String getValue(final Location location, final String type ) {
        if (!this.spawnerExists(location)) return null;

        try (final PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                .prepareStatement("SELECT * FROM `" + SpawnerTools.getPlugin().getServerName() + "` WHERE `Location` = '" + SpawnerTools.getPlugin().getManager().toMySQLString(location) + "'");

             final ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getString(type);
            }

        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public List<String> getListValue(final UUID uuid, final String type ) {
        final List<String> list = new ArrayList<>();
        try (final PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                .prepareStatement("SELECT * FROM `" + SpawnerTools.getPlugin().getServerName() + "` WHERE `UUID` = '" + uuid.toString() + "'");

             final ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                list.add(resultSet.getString(type));
            }
            
            return list;

        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }
    
    public int getIntValue(final Location location, final String type) {
        if (!this.spawnerExists(location)) return 0;

        try (final PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                .prepareStatement("SELECT * FROM `" + SpawnerTools.getPlugin().getServerName() + "` WHERE `Location` = '" + SpawnerTools.getPlugin().getManager().toMySQLString(location) + "'");

             final ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getInt(type);
            }

        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    public void setIntValue(final Location location, final String type, final int value) {
        Bukkit.getScheduler().runTaskAsynchronously(SpawnerTools.getPlugin(), () -> {
            if (!spawnerExists(location)) return;

            try (final PreparedStatement statement = SpawnerTools.getPlugin().getMySQL().getConnection()
                    .prepareStatement("UPDATE `" + SpawnerTools.getPlugin().getServerName() + "` SET `" + type + "` = " + value + " WHERE `Location` = '" + SpawnerTools.getPlugin().getManager().toMySQLString(location) + "'")) {

                statement.executeUpdate();

            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void checkConnection() {
        try {
            if (!isConnected() || !this.connection.isValid(10) || this.connection.isClosed()) connect();
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (final SQLException exception) {
            exception.printStackTrace();
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
