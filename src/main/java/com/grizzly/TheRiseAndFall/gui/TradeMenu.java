package com.grizzly.TheRiseAndFall.gui;

import com.grizzly.TheRiseAndFall.Main;
import com.grizzly.TheRiseAndFall.util.ItemMaker;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class TradeMenu implements InventoryHolder {

    Main plugin = Main.plugin;

    final Integer id;
    final Player initiator;
    final Player receiver;
    boolean initiatorReady;
    boolean receiverReady;
    ArrayList<ItemStack> initiatorItems;
    ArrayList<ItemStack> receiverItems;
    int countDown;



    public TradeMenu(Player initiator, Player receiver) {
        if (exists(initiator, receiver)) {
            id = getId(initiator, receiver);
            this.initiator = db.currentTrades.get(id).initiator;
            this.receiver = db.currentTrades.get(id).receiver;
            initiatorReady = db.currentTrades.get(id).initiatorReady;
            receiverReady = db.currentTrades.get(id).receiverReady;
            initiatorItems = db.currentTrades.get(id).initiatorItems;
            receiverItems = db.currentTrades.get(id).receiverItems;
            countDown = db.currentTrades.get(id).countDown;
        } else {
            id = generateId();
            this.initiator = initiator;
            this.receiver = receiver;
            initiatorReady = false;
            receiverReady = false;
            initiatorItems = new ArrayList<>();
            receiverItems = new ArrayList<>();
            countDown = -1;
        } save();
    }

    public TradeMenu(Player player, boolean save) {
        if (exists(player, null)) {
            id = getId(player, null);
            initiator = db.currentTrades.get(id).initiator;
            receiver = db.currentTrades.get(id).receiver;
            initiatorReady = db.currentTrades.get(id).initiatorReady;
            receiverReady = db.currentTrades.get(id).receiverReady;
            countDown = db.currentTrades.get(id).countDown;
            initiatorItems = db.currentTrades.get(id).initiatorItems;
            receiverItems = db.currentTrades.get(id).receiverItems;
        } else {
            id = generateId();
            initiator = player;
            receiver = null;
            initiatorReady = false;
            receiverReady = false;
            initiatorItems = new ArrayList<>();
            receiverItems = new ArrayList<>();
            countDown = -1;
        } if (save) save();
    }


    void save() { db.currentTrades.put(id, this); }


    public Player getInitiator() { return initiator; }
    public Player getReceiver() { return receiver; }
    public int getCountDown() { return countDown; }


    public void playerQuit(Boolean initiatorExiting) {
        stopCountdown(initiator);
        initiator.sendMessage(Plugin.prefix + "§cThe trade has been exited!");
        receiver.sendMessage(Plugin.prefix + "§cThe trade has been exited!");
        for (ItemStack item : initiatorItems) initiator.getInventory().addItem(item);
        for (ItemStack item : receiverItems) receiver.getInventory().addItem(item);
        db.currentTrades.remove(id);
        if (initiatorExiting) getReceiver().closeInventory();
        else getInitiator().closeInventory();
    }

    public void tradeComplete() {
        initiator.sendMessage(Plugin.prefix + "§aThe trade has completed!");
        receiver.sendMessage(Plugin.prefix + "§aThe trade has completed!");
        for (ItemStack item : receiverItems) initiator.getInventory().addItem(item);
        for (ItemStack item : initiatorItems) receiver.getInventory().addItem(item);
        db.currentTrades.remove(id);
        getReceiver().closeInventory();
        getInitiator().closeInventory();
    }

    public void tradeCompleteNoInventory() {
        initiator.sendMessage(Plugin.prefix + "§cOne player did not have enough inventory space to trade!");
        receiver.sendMessage(Plugin.prefix + "§cOne player did not have enough inventory space to trade!");
        for (ItemStack item : initiatorItems) initiator.getInventory().addItem(item);
        for (ItemStack item : receiverItems) receiver.getInventory().addItem(item);
        db.currentTrades.remove(id);
        getReceiver().closeInventory();
        getInitiator().closeInventory();
    }

    public void open() {
        initiator.openInventory(getInventory());
        receiver.openInventory(getInventory());
        update();
    }

    public void update() {
        update(initiator, initiatorReady, receiverReady, initiatorItems, receiverItems);
        update(receiver, receiverReady, initiatorReady, receiverItems, initiatorItems);
    }

    public void toggleReady(Player player) {
        if (db.currentTrades.get(id).initiator == player) {
            initiatorReady = !initiatorReady;
            if (!initiatorReady) receiverReady = false;
        } if (db.currentTrades.get(id).receiver == player) {
            receiverReady = !receiverReady;
            if (!receiverReady) initiatorReady = false;
        } update();
        if (receiverReady && initiatorReady) startCountdown(initiator);
        if (!receiverReady && !initiatorReady) stopCountdown(initiator);
    }

    public void addItemToSide(Player player, ItemStack item, Integer slot) {
        if (db.currentTrades.get(id).initiator == player && initiatorItems.size() != 16) {
            initiatorItems.add(item);
            receiverReady = false;
            initiatorReady = false;
            save();
            stopCountdown(initiator);
            getInitiator().getInventory().setItem(slot, null);
            update();
        } if (db.currentTrades.get(id).receiver == player && receiverItems.size() != 16) {
            receiverItems.add(item);
            receiverReady = false;
            initiatorReady = false;
            save();
            stopCountdown(initiator);
            getReceiver().getInventory().setItem(slot, null);
            update();
        }
    }

    public void removeItemFromSide(Player player, ItemStack item) {
        if (db.currentTrades.get(id).initiator == player && !item.getItemMeta().getDisplayName().equals("§f")) {
            initiatorItems.remove(item);
            receiverReady = false;
            initiatorReady = false;
            save();
            stopCountdown(initiator);
            getInitiator().getInventory().addItem(item);
            update();
        } if (db.currentTrades.get(id).receiver == player && !item.getItemMeta().getDisplayName().equals("§f")) {
            receiverItems.remove(item);
            receiverReady = false;
            initiatorReady = false;
            save();
            stopCountdown(initiator);
            getReceiver().getInventory().addItem(item);
            update();
        }
    }


    void startCountdown(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getCountDown() == -1) db.runningTradeCountdowns.put(player.getUniqueId(), getTaskId());
                countDown++;
                update();
                if (countDown == 3) {
                    db.runningTradeCountdowns.remove(player.getUniqueId());
                    cancel();
                    int initiatorSpots = 0;
                    int receiverSpots = 0;
                    for (ItemStack item : initiator.getInventory().getStorageContents()) if (item == null) initiatorSpots++;
                    for (ItemStack item : receiver.getInventory().getStorageContents()) if (item == null) receiverSpots++;
                    if (initiatorSpots >= receiverItems.size() && receiverSpots >= initiatorItems.size()) tradeComplete();
                    else tradeCompleteNoInventory();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    void stopCountdown(Player player) {
        if (db.runningTradeCountdowns.containsKey(player.getUniqueId())) {
            countDown = -1;
            save();
            Bukkit.getScheduler().cancelTask(db.runningTradeCountdowns.get(player.getUniqueId()));
            db.runningTradeCountdowns.remove(player.getUniqueId());
            update();
        }
    }


    Boolean exists(Player player1, Player player2) {
        for (int id : db.currentTrades.keySet()) if (db.currentTrades.get(id).initiator == player1 || db.currentTrades.get(id).receiver == player1 ||
                    db.currentTrades.get(id).initiator == player2 || db.currentTrades.get(id).receiver == player2) return true;
        return false;
    }

    Integer getId(Player player1, Player player2) {
        for (int id : db.currentTrades.keySet()) if (db.currentTrades.get(id).initiator == player1 || db.currentTrades.get(id).receiver == player1 ||
                db.currentTrades.get(id).initiator == player2 || db.currentTrades.get(id).receiver == player2) return id;
        return -1;
    }

    Integer generateId() {
        int id = 0;
        while (db.currentTrades.containsKey(id)) id++;
        return id;
    }

    public void update(Player player, Boolean p1Ready, Boolean p2Ready, ArrayList<ItemStack> player1Items, ArrayList<ItemStack> player2Items) {
        try {
            Inventory inv = player.getOpenInventory().getTopInventory();
            for (int i = 0; i < 54; i++) inv.setItem(i, new ItemMaker().createBlank());
            for (int i = 36; i < 45; i++) inv.setItem(i, new ItemMaker().createBlank());
            String p1Text = "§cClick to accept!";
            Material p1Mat = Material.RED_STAINED_GLASS_PANE;
            if (p1Ready) {
                p1Text = "§aClick to cancel!";
                p1Mat = Material.LIME_STAINED_GLASS_PANE;
            }
            String p2Text = "§cOther player has not yet accepted!";
            Material p2Mat = Material.RED_STAINED_GLASS_PANE;
            if (p2Ready) {
                p2Text = "§aOther player has accepted!";
                p2Mat = Material.LIME_STAINED_GLASS_PANE;
            }
            for (int i = 45; i < 49; i++) {
                inv.setItem(i, new ItemMaker().create(p1Text, p1Mat, null));
                inv.setItem(i + 5, new ItemMaker().create(p2Text, p2Mat, null));
            }
            inv.setItem(49, new ItemMaker().createBlank());
            Material mat = Material.BLACK_STAINED_GLASS_PANE;
            String name = "§f";
            switch (countDown) {
                case 0:
                    mat = Material.ORANGE_STAINED_GLASS_PANE;
                    name = "§6Confirming... (3)";
                    break;
                case 1:
                    mat = Material.YELLOW_STAINED_GLASS_PANE;
                    name = "§eConfirming... (2)";
                    break;
                case 2:
                    mat = Material.GREEN_STAINED_GLASS_PANE;
                    name = "§aConfirming... (1)";
                    break;
            }
            for (int i = 0; i < 4; i++) inv.setItem(i * 9 + 4, new ItemMaker().create(name, mat, null));
            for (int i = 0; i < player1Items.size(); i++) {
                int slotNum = new Integer[]{0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30}[i];
                inv.setItem(slotNum, player1Items.get(i));
            }
            for (int i = 0; i < player2Items.size(); i++) {
                int slotNum = new Integer[]{5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35}[i];
                inv.setItem(slotNum, player2Items.get(i));
            }
        } catch (Exception ignored) {}
    }


    Inventory createInventory() { return Bukkit.createInventory(this, 54, "§3§lTrade"); }

    @Override
    public Inventory getInventory() { return createInventory(); }
}
