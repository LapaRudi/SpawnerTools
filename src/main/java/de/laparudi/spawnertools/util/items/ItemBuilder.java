package de.laparudi.spawnertools.util.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(final Material material) {
        this.item = new ItemStack(material);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder(final ItemStack item) {
        this.item = item;
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder setName(final String name) {
        if (meta == null) return this;
        meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder setAmount(final int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder setLore(final List<String> lore) {
        if (meta == null || lore == null) return this;
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder setLore(final String... lore) {
        if (meta == null) return this;
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder addLore(final String... lines) {
        if (meta == null) return this;
        final List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.addAll(Arrays.asList(lines));
        meta.setLore(lore);
        return this;
    }
    
    public ItemBuilder setSkullOwner(final String owner) {
        final SkullMeta skullMeta = (SkullMeta) meta;

        if (Bukkit.getBukkitVersion().contains("1.8.8")) {
            skullMeta.setOwner(owner);
            
        } else {
            final Player player = Bukkit.getPlayer(owner);
            if (player == null) return this;
            skullMeta.setOwningPlayer(player);
        }
        
        return this;
    }
    
    public ItemBuilder addFlag() {
        if (meta == null) return this;
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        return this;
    }
    
    public boolean hasFlag() {
        return meta.hasItemFlag(ItemFlag.HIDE_POTION_EFFECTS);
    }

    public ItemStack toItem() {
        item.setItemMeta(meta);
        return item;
    }
}
