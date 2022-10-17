package com.grizzly.TheRiseAndFall.events;

import com.grizzly.TheRiseAndFall.gui.TradeMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class TradeEvents implements Listener {

    @EventHandler
    public void onClickItem(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (player.getOpenInventory().getTitle().equals("§3§lTrade")) {
            if (e.getClick() != ClickType.DOUBLE_CLICK && e.getClick() != ClickType.NUMBER_KEY && e.getClick() != ClickType.DROP && e.getClick() != ClickType.CONTROL_DROP) {
                for (int i = 45; i < 49; i++) if (e.getRawSlot() == i) new TradeMenu(player, true).toggleReady(player);
                for (int i = 54; i < 90; i++) if (e.getRawSlot() == i && player.getInventory().getItem(translateSlotNumber(i)) != null) {
                    new TradeMenu(player, true).addItemToSide(player, player.getInventory().getItem(translateSlotNumber(i)), translateSlotNumber(i));
                } for (int i : new Integer[]{0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30})
                    if (e.getRawSlot() == i) {
                        new TradeMenu(player, true).removeItemFromSide(player, player.getOpenInventory().getTopInventory().getItem(i));
                }
            } e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (player.getOpenInventory().getTitle().equals("§3§lTrade")) {
            for (int id : db.currentTrades.keySet()) {
                if (db.currentTrades.get(id).getInitiator() == player) {
                    new TradeMenu(player, true).playerQuit(true);
                    return;
                } if (db.currentTrades.get(id).getReceiver() == player) {
                    new TradeMenu(player, true).playerQuit(false);
                    return;
                }
            }
        }
    }

    Integer translateSlotNumber(Integer slotNum) {
        if (slotNum > 53 && slotNum < 81) return slotNum - 45;
        else return slotNum - 81;
    }
}
