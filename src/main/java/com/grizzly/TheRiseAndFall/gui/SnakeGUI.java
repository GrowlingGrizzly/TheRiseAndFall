package com.grizzly.TheRiseAndFall.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SnakeGUI implements InventoryHolder {

    Inventory createInventory() { return Bukkit.createInventory(this, 54, "§2§lSnake"); }
    Inventory createSpeedInventory() { return Bukkit.createInventory(this, 27, "§6§lSelect your speed"); }

    @Override
    public Inventory getInventory() { return createInventory(); }

    public Inventory getSpeedInventory() { return createSpeedInventory(); }
}
