package com.grizzly.TheRiseAndFall.events;

import com.grizzly.TheRiseAndFall.Main;
import com.grizzly.TheRiseAndFall.gui.ShopGUI;
import com.grizzly.TheRiseAndFall.util.ItemMaker;
import com.grizzly.TheRiseAndFall.util.Plugin;
import com.grizzly.TheRiseAndFall.util.Shop;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class ShopEvents implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (db.changingShopPrice.containsKey(player.getUniqueId())) {
            if (StringUtils.isNumeric(e.getMessage())) {
                if (Integer.parseInt(e.getMessage()) > 128) return;
                db.getShop(db.changingShopPrice.get(player.getUniqueId())).setPrice(Integer.parseInt(e.getMessage()));
                player.sendMessage(Plugin.prefix + "§aShop diamond amount changed!");
                Bukkit.getScheduler().runTask(Main.plugin, () -> {
                    db.getShop(db.changingShopPrice.get(player.getUniqueId())).open();
                    db.getShop(db.changingShopPrice.get(player.getUniqueId())).updateDisplay(false);
                    db.changingShopPrice.remove(player.getUniqueId());
                });
                e.setCancelled(true);
            } else if (e.getMessage().equalsIgnoreCase("cancel")) {
                player.sendMessage(Plugin.prefix + "§cShop change chancelled!");
                e.setCancelled(true);
            }
        } else if (db.changingShopAmount.containsKey(player.getUniqueId())) {
            if (StringUtils.isNumeric(e.getMessage())) {
                if (Integer.parseInt(e.getMessage()) > 2304) return;
                db.getShop(db.changingShopAmount.get(player.getUniqueId())).setAmount(Integer.parseInt(e.getMessage()));
                player.sendMessage(Plugin.prefix + "§aShop item amount changed!");
                Bukkit.getScheduler().runTask(Main.plugin, () -> {
                    db.getShop(db.changingShopAmount.get(player.getUniqueId())).open();
                    db.getShop(db.changingShopAmount.get(player.getUniqueId())).updateDisplay(false);
                    db.changingShopAmount.remove(player.getUniqueId());
                });
                e.setCancelled(true);
            } else if (e.getMessage().equalsIgnoreCase("cancel")) {
                player.sendMessage(Plugin.prefix + "§cShop change chancelled!");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClickItem(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (player.getOpenInventory().getTitle().equals("§aShop - Owner")) {
            int shopId = new ShopGUI().getOpenShopId(player);
            if (e.getRawSlot() == 4) {
                db.getShop(shopId).removeShopItem(player);
            } if (e.getRawSlot() == 10) {
                db.getShop(shopId).swapSelling();
                new ShopGUI().updateOwner(player, shopId);
                db.getShop(shopId).updateDisplay(false);
            } if (e.getRawSlot() == 12) {
                if (e.getClick().isLeftClick()) new ShopGUI().setShopPrice(player, shopId);
                else if (e.getClick().isRightClick()) new ShopGUI().setShopAmount(player, shopId);
            } if (e.getRawSlot() == 14) {
                new ShopGUI().openItemStorage(player, shopId);
            } if (e.getRawSlot() == 16) {
                new ShopGUI().openDiamondStorage(player, shopId);
            } if (e.getRawSlot() == 26) new Shop(player, shopId).remove(false);
            if (e.getRawSlot() > 26 && (e.getClick().isLeftClick() || e.getClick().isRightClick())) {
                if (db.getShop(shopId).item.getType().equals(Material.BARRIER)) {
                    if (e.getRawSlot() > 53) db.getShop(shopId).setShopItem(player, e.getRawSlot() - 54);
                    else db.getShop(shopId).setShopItem(player, e.getRawSlot() - 18);
                }
            } e.setCancelled(true);
            return;
        } if (player.getOpenInventory().getTitle().equals("§aShop - Item Storage")) {
            if (e.getClick().isKeyboardClick()) e.setCancelled(true);
            int shopId = new ShopGUI().getOpenShopId(player);
            if (e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(db.getShop(shopId).item.getType())) e.setCancelled(true);
            return;
        } if (player.getOpenInventory().getTitle().equals("§aShop - Diamond Storage")) {
            if (e.getClick().isKeyboardClick()) e.setCancelled(true);
            if (e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(Material.DIAMOND)) e.setCancelled(true);
            return;
        } if (player.getOpenInventory().getTitle().equals("§aShop - Customer")) {
            int shopId = new ShopGUI().getOpenShopId(player);
            if (e.getRawSlot() == 11 && (e.getClick().equals(ClickType.LEFT) || e.getClick().equals(ClickType.RIGHT) || e.getClick().equals(ClickType.MIDDLE))) {
                PlayerInventory inv = player.getInventory();
                Shop shop = db.getShop(shopId);
                ItemStack[] storage = inv.getStorageContents().clone();
                int itemAmount = 0;
                int emptySlots = 0;
                String ifNeedsSAdd = " Diamond";
                if (shop.price > 1) ifNeedsSAdd = " Diamonds";
                if (shop.isSelling) {
                    for (ItemStack item : storage) {
                        if (item == null || item.getType().equals(Material.AIR)) emptySlots++;
                        else if (item.getType().equals(Material.DIAMOND)) itemAmount += item.getAmount();
                    } if (e.getClick().equals(ClickType.LEFT)) {
                        if (itemAmount >= shop.price) {
                            if (emptySlots >= (shop.amount / 64)) {
                                if (shop.itemStorage >= shop.amount) {
                                    if (!((shop.diamondStorage + shop.price) > 3456)) {
                                        player.getInventory().removeItem(new ItemMaker().create("", Material.DIAMOND, null, shop.price));
                                        player.getInventory().addItem(new ItemMaker().create("", shop.item.getType(), null, shop.amount));
                                        shop.setStorage(true, shop.itemStorage - shop.amount);
                                        shop.setStorage(false, shop.diamondStorage + shop.price);
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                                        player.sendMessage(Plugin.prefix + "§aBought §e" + shop.amount + "x " +
                                                Plugin.firstLetterCapital(shop.item.getType().toString(), true) + " §afor §b" + shop.price + ifNeedsSAdd + "§a!");
                                    } else shopFail(player, "FULL_STOCK", null);
                                } else shopFail(player, "NO_STOCK", null);
                            } else shopFail(player, "NO_INV", null);
                        } else shopFail(player, "NO_DIA", null);
                    } if (e.getClick().equals(ClickType.MIDDLE)) {
                        int maxStacksSold = itemAmount / shop.price;
                        if (maxStacksSold == 0) {
                            shopFail(player, "NO_DIA", null);
                            e.setCancelled(true);
                            return;
                        } while ((maxStacksSold * shop.amount) > shop.itemStorage) maxStacksSold--;
                        if (maxStacksSold > 0) {
                            while ((shop.diamondStorage + (maxStacksSold * shop.price) > 3456)) maxStacksSold--;
                            if (maxStacksSold > 0) {
                                player.getInventory().removeItem(new ItemMaker().create("", Material.DIAMOND, null, maxStacksSold * shop.price));
                                player.getInventory().addItem(new ItemMaker().create("", shop.item.getType(), null, maxStacksSold * shop.amount));
                                shop.setStorage(true, shop.itemStorage - maxStacksSold * shop.amount);
                                shop.setStorage(false, shop.diamondStorage + maxStacksSold * shop.price);
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                                player.sendMessage(Plugin.prefix + "§aBought §e" + maxStacksSold * shop.amount + "x " +
                                        Plugin.firstLetterCapital(shop.item.getType().toString(), true) + " §afor §b" + maxStacksSold * shop.price + ifNeedsSAdd + "§a!");
                            } else shopFail(player, "FULL_STOCK", null);
                        } else shopFail(player, "NO_STOCK", null);
                    }
                } else {
                    for (ItemStack item : storage) {
                        if (item == null || item.getType().equals(Material.AIR)) emptySlots++;
                        else if (item.getType().equals(shop.item.getType())) itemAmount += item.getAmount();
                    } if (e.getClick().equals(ClickType.LEFT)) {
                        if (itemAmount >= shop.amount) {
                            if (emptySlots >= 1) {
                                if (shop.diamondStorage >= shop.price) {
                                    if (!((shop.itemStorage + shop.amount) > 3456)) {
                                        player.getInventory().removeItem(new ItemMaker().create("", shop.item.getType(), null, shop.amount));
                                        player.getInventory().addItem(new ItemMaker().create("", Material.DIAMOND, null, shop.price));
                                        shop.setStorage(true, shop.itemStorage + shop.amount);
                                        shop.setStorage(false, shop.diamondStorage - shop.price);
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                                        player.sendMessage(Plugin.prefix + "§aSold §e" + shop.amount + "x " +
                                                Plugin.firstLetterCapital(shop.item.getType().toString(), true) + " §afor §b" + shop.price + ifNeedsSAdd + "§a!");
                                    } else shopFail(player, "FULL_STOCK", null);
                                } else shopFail(player, "NO_STOCK", null);
                            } else shopFail(player, "NO_INV", null);
                        } else shopFail(player, "NO_ITEM", Plugin.firstLetterCapital(shop.item.getType().toString(), true));
                    } if (e.getClick().equals(ClickType.MIDDLE)) {
                        int maxStacksSold = itemAmount / shop.amount;
                        if (maxStacksSold == 0) {
                            shopFail(player, "NO_ITEM", Plugin.firstLetterCapital(shop.item.getType().toString(), true));
                            e.setCancelled(true);
                            return;
                        } while ((maxStacksSold * shop.price) > shop.diamondStorage) maxStacksSold--;
                        if (maxStacksSold > 0) {
                            while ((shop.itemStorage + (maxStacksSold * shop.amount) > 3456)) maxStacksSold--;
                            if (maxStacksSold > 0) {
                                player.getInventory().removeItem(new ItemMaker().create("", shop.item.getType(), null, maxStacksSold * shop.amount));
                                player.getInventory().addItem(new ItemMaker().create("", Material.DIAMOND, null, maxStacksSold * shop.price));
                                shop.setStorage(true, shop.itemStorage + maxStacksSold * shop.amount);
                                shop.setStorage(false, shop.diamondStorage - maxStacksSold * shop.price);
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                                player.sendMessage(Plugin.prefix + "§aSold §e" + maxStacksSold * shop.amount + "x " +
                                        Plugin.firstLetterCapital(shop.item.getType().toString(), true) + " §afor §b" + maxStacksSold * shop.price + ifNeedsSAdd + "§a!");
                            } else shopFail(player, "FULL_STOCK", null);
                        } else shopFail(player, "NO_STOCK", null);
                    }
                }
            } if (e.getRawSlot() == 15) {
                new ShopGUI().openCustomerStock(player, shopId);
            } e.setCancelled(true);
            return;
        } if (player.getOpenInventory().getTitle().equals("§aShop - Stock")) e.setCancelled(true);
    }

    void shopFail(Player player, String type, String item) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        switch(type) {
            case "NO_STOCK": player.sendMessage(Plugin.prefix + "§cShop has insufficient stock!"); break;
            case "FULL_STOCK": player.sendMessage(Plugin.prefix + "§cShop stock is full!"); break;
            case "NO_INV": player.sendMessage(Plugin.prefix + "§cYou do not have enough inventory space!"); break;
            case "NO_DIA": player.sendMessage(Plugin.prefix + "§cYou do not have enough diamonds to purchase this!"); break;
            case "NO_ITEM": player.sendMessage(Plugin.prefix + "§cYou do not have enough §e" + item + " §cto sell!"); break;
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e) {
        Main plugin = Main.plugin;
        Player player = (Player) e.getPlayer();
        if (player.getOpenInventory().getTitle().equals("§aShop - Item Storage")) {
            ItemStack[] items = player.getOpenInventory().getTopInventory().getContents().clone();
            int amount = 0;
            for (ItemStack item : items) if (item != null) amount += item.getAmount();
            db.getShop(new ShopGUI().getOpenShopId(player)).setStorage(true, amount);
            db.editingItemStock.remove(new ShopGUI().getOpenShopId(player));
            Bukkit.getScheduler().runTask(plugin, () -> new ShopGUI().openOwner(player, new ShopGUI().getOpenShopId(player)));
        } if (player.getOpenInventory().getTitle().equals("§aShop - Diamond Storage")) {
            ItemStack[] items = player.getOpenInventory().getTopInventory().getContents().clone();
            int amount = 0;
            for (ItemStack item : items) if (item != null) amount += item.getAmount();
            db.getShop(new ShopGUI().getOpenShopId(player)).setStorage(false, amount);
            db.editingDiamondStock.remove(new ShopGUI().getOpenShopId(player));
            Bukkit.getScheduler().runTask(plugin, () -> new ShopGUI().openOwner(player, new ShopGUI().getOpenShopId(player)));
        } if (player.getOpenInventory().getTitle().equals("§aShop - Stock")) {
            Bukkit.getScheduler().runTask(plugin, () -> new ShopGUI().openShopper(player, new ShopGUI().getOpenShopId(player)));
        } if (player.getOpenInventory().getTitle().equals("§aShop - Owner")) new ShopGUI().closeShop(player);

    }

}
