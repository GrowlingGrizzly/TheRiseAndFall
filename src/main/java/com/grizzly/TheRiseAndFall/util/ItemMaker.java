package com.grizzly.TheRiseAndFall.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemMaker {

    public ItemStack create(String name, Material mat, List<String> lore, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack create(String name, Material mat, List<String> lore) {
        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createBlank() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f");
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack head(String name) {
        Configs config = Configs.configs;
        switch(name) {
            case "up": return config.getHeads().getItemStack("Heads.Up");
            case "down": return config.getHeads().getItemStack("Heads.Down");
            case "left": return config.getHeads().getItemStack("Heads.Left");
            case "right": return config.getHeads().getItemStack("Heads.Right");
            default: return config.getHeads().getItemStack("Heads.Blank");
        }
    }

}
