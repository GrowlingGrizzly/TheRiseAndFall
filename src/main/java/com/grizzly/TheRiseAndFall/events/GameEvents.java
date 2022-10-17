package com.grizzly.TheRiseAndFall.events;

import com.grizzly.TheRiseAndFall.Main;
import com.grizzly.TheRiseAndFall.games.Snake;
import com.grizzly.TheRiseAndFall.gui.GameMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GameEvents implements Listener {

    Main plugin = Main.plugin;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClickItem(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked(); if (player.getOpenInventory().getTitle().equals("§9§lGames")) {
            if (e.getRawSlot() == 12) new Snake(player).selectSpeed();
            if (e.getRawSlot() == 14) player.sendMessage("§ctetris§f.§einitiate(§a" + player.getName() + "§e)");
            e.setCancelled(true);
        } if (player.getOpenInventory().getTitle().equals("§2§lSnake")) {
            if (e.getRawSlot() == 58) new Snake(player).setDir(0);
            if (e.getRawSlot() == 66) new Snake(player).setDir(2);
            if (e.getRawSlot() == 68) new Snake(player).setDir(3);
            if (e.getRawSlot() == 76) new Snake(player).setDir(1);
            e.setCancelled(true);
        } if (player.getOpenInventory().getTitle().equals("§6§lSelect your speed")) {
            if (e.getRawSlot() == 11) new Snake(player).start(6);
            if (e.getRawSlot() == 13) new Snake(player).start(4);
            if (e.getRawSlot() == 15) new Snake(player).start(2);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (player.getOpenInventory().getTitle().equals("§2§lSnake")) new Snake(player).quit();
        if (player.getOpenInventory().getTitle().equals("§6§lSelect your speed")) if (new Snake(player).getSpeed() == 0)
            Bukkit.getScheduler().runTask(plugin, () -> new GameMenu().open(player));
    }

}
