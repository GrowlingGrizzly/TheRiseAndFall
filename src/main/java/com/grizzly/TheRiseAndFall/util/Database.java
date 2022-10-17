package com.grizzly.TheRiseAndFall.util;

import com.grizzly.TheRiseAndFall.games.Snake;
import com.grizzly.TheRiseAndFall.gui.TradeMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.*;

public class Database {

    public static Database db = new Database();
    Configs config = Configs.configs;

    public Database() { db = this; }

    public HashMap<Integer, TradeMenu> currentTrades = new HashMap<>();
    public HashMap<UUID, Integer> runningTradeCountdowns = new HashMap<>();
    public HashMap<UUID, Integer> runningAltarBoundaries = new HashMap<>();
    public HashMap<UUID, Altar> currentAltars = new HashMap<>();
    public HashMap<UUID, Snake> runningSnakeGUIs = new HashMap<>();
    public HashMap<UUID, Integer> runningSnakeGames = new HashMap<>();
    public HashMap<Integer, Claim> claims = new HashMap<>();
    HashMap<Integer, Shop> shops = new HashMap<>();
    public ArrayList<UUID> readyForAltar = new ArrayList<>();


    public Shop getShop(int id) { return shops.get(id); }
    public void removeShop(int id) { shops.remove(id); }
    public void setShop(int id, Shop shop) { shops.put(id, shop); }
    public Set<Integer> getShopIds() { return shops.keySet(); }
    public HashMap<UUID, Integer> changingShopAmount = new HashMap<>();
    public HashMap<UUID, Integer> changingShopPrice = new HashMap<>();
    public ArrayList<Integer> editingDiamondStock = new ArrayList<>();
    public ArrayList<Integer> editingItemStock = new ArrayList<>();

    public void cacheClaims() {
        for (String claimNum : Configs.configs.getClaims().getKeys(false))
            new Claim(Integer.parseInt(claimNum.replaceAll("claim", ""))).cache();
    }
    
    public void saveInventory(Player player) {
        ItemStack[] items = player.getInventory().getStorageContents();
        config.getInvBackup().set(player.getUniqueId().toString(), items);
        config.saveInvBackup();
        player.getInventory().setStorageContents(null);
        player.setCanPickupItems(false);
    }

    public void loadInventory(Player player) {
        player.getInventory().setStorageContents(null);
        @SuppressWarnings("unchecked") List<ItemStack> itemList = (List<ItemStack>) config.getInvBackup().get(player.getUniqueId().toString());
        ItemStack[] items = itemList.toArray(new ItemStack[0]);
        player.getInventory().setStorageContents(items);
        config.getInvBackup().set(player.getUniqueId().toString(), null);
        config.saveInvBackup();
        player.setCanPickupItems(true);
    }

    public boolean inventoryBackedUp(Player player) { return config.getInvBackup().isSet(player.getUniqueId().toString()); }

    public void loadInventoryIfBackedUp(Player player) { if (config.getInvBackup().isSet(player.getUniqueId().toString()) && player.isOnline()) loadInventory(player); }

    public String formattedTime() {
        LocalDateTime time = LocalDateTime.now();
        int hour = time.getHour();
        String amPm = "AM";
        if (hour > 11 && hour != 24) {
            amPm = "PM";
            if (hour > 12) hour -= 12;
        } String add0ToMinutes = "";
        if (time.getMinute() < 10) add0ToMinutes = "0";
        return time.getMonthValue() + "/" + time.getDayOfMonth() + "/" + time.getYear() + " " + hour + ":" + add0ToMinutes + time.getMinute() + " " + amPm;
    }
    
    
    
}
