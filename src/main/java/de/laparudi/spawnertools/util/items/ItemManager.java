package de.laparudi.spawnertools.util.items;

import com.cryptomorin.xseries.XMaterial;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemManager {
    
    public final ItemStack GRAY_GLASS = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").toItem();
    
    public final ItemStack REMOVE_OK = new ItemBuilder(Material.EMERALD).setName("§3Spawner abbauen").setLore(
            "",
            "§7» §bWenn du deinen Spawner abbaust, bekommst du",
            "§7» §bden Spawner und das Spawn-Ei in dein Inventar.",
            "",
            "§7» §cDie Statistiken des Spawners werden zurückgesetzt.",
            "")
            .addFlag().toItem();
    
    public final ItemStack REMOVE_PLOT_OWNER = new ItemBuilder(Material.EMERALD).setName("§3Spawner abbauen").setLore(
            "",
            "§7» §bWenn du deinen Spawner abbaust, bekommst du",
            "§7» §bden Spawner und das Spawn-Ei in dein Inventar.",
            "",
            "§7» §9Weil du der Plot-Besitzer bist,",
            "§7» §9kannst du den Spawner abbauen.",
            "")
            .addFlag().toItem();
    
    public final ItemStack REMOVE_TRUSTED = new ItemBuilder(Material.EMERALD).setName("§3Spawner abbauen").setLore(
            "",
            "§7» §bWenn du deinen Spawner abbaust, bekommst du",
            "§7» §bden Spawner und das Spawn-Ei in dein Inventar.",
            "",
            "§7» §9Weil dieser Spawner dir gehört, und du auf diesem",
            "§7» §9Grundstück vertraut bist kannst du den Spawner abbauen.",
            "")
            .addFlag().toItem();
    
    public final ItemStack REMOVE_NOTRUST = new ItemBuilder(Material.REDSTONE).setName("§3Spawner abbauen").setLore(
            "",
            "§7» §cAuch wenn dieser Spawner dir gehört,",
            "§7» §cdarfst du ihn nur abbauen, wenn du",
            "§7» §cauf diesem Grundstück vertraut bist.",
            "")
            .toItem();
    
    public final ItemStack REMOVE_BLOCKED = new ItemBuilder(Material.REDSTONE).setName("§3Spawner abbauen").setLore(
            "",
            "§7» §cNur der Besitzer des Spawners kann ihn abbauen.",
            "")
            .toItem();
    
    public ItemStack SPAWNER_STATS(final String mob, final String spawns, final Location location) {
        return new ItemBuilder(Material.BOOK).setName("§3Statisiken").setLore(
                "",
                "§7» §3Mob: §b" + this.toMobString(mob),
                "§7» §3Spawns: §b" + spawns,
                "§7» §3Location: §b" + this.toMySQLString(location),
                "")
                .toItem();
    }
    
    public ItemStack CUSTOM_HEAD(final String owner) {
        return new ItemBuilder(XMaterial.PLAYER_HEAD.parseItem()).setName("§3Besitzer").setLore(
                "",
                "§7» §bDieser Spawner gehört §3" + owner + "§b.",
                "")
                .setSkullOwner(owner).toItem();
    }

    public String toMySQLString(final Location location) {
        if (location == null || location.getWorld() == null) return "";
        return location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }

    public Location fromMySQLString(final String string) {
        final String[] values = string.split(" ");
        return new Location(Bukkit.getWorld(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3]));
    }

    public String toMobString(final String type) {
        return WordUtils.capitalize(type.toLowerCase().replace('_', ' '));
    }
}
