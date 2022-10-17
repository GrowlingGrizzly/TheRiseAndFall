package com.grizzly.TheRiseAndFall.gui;

import com.grizzly.TheRiseAndFall.util.ItemMaker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;

public class GameMenu implements InventoryHolder {

    public void open(Player player) {
        player.openInventory(getInventory());
        if (player.getOpenInventory().getTitle().equals("§9§lGames")) {
            Inventory screen = player.getOpenInventory().getTopInventory();
            for (int i = 0; i < 27; i++) screen.setItem(i, new ItemMaker().createBlank());
            screen.setItem(12, new ItemMaker().create("§2Snake", Material.GREEN_WOOL, Arrays.asList("§7§oClick here to", "§7§oplay Snake!")));
            screen.setItem(14, new ItemMaker().create("§bTetris", Material.OXIDIZED_CUT_COPPER_STAIRS, Arrays.asList("§7§oClick here to", "§7§oplay Tetris!")));
        }
    }

    Inventory createInventory() { return Bukkit.createInventory(this, 27, "§9§lGames"); }

    @Override
    public Inventory getInventory() { return createInventory(); }
}
