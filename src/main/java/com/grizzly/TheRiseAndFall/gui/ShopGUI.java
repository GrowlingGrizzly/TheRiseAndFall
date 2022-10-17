package com.grizzly.TheRiseAndFall.gui;

import com.grizzly.TheRiseAndFall.util.ItemMaker;
import com.grizzly.TheRiseAndFall.util.Plugin;
import com.grizzly.TheRiseAndFall.util.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class ShopGUI implements InventoryHolder {

    ItemMaker items = new ItemMaker();
    static HashMap<UUID, Integer> openShops = new HashMap<>();

    public void openOwner(Player player, int id) {
        try {
            player.openInventory(getInventory());
            openShops.put(player.getUniqueId(), id);
            updateOwner(player, id);
        } catch (Exception e) {
            player.closeInventory();
            player.sendMessage(Plugin.prefix + "§cAn error occured when attempting to open shop.");
            closeShop(player);
            e.printStackTrace();
        }
    }

    public void openShopper(Player player, int id) {
        try {
            if (db.getShop(id).item.getType().equals(Material.BARRIER)) player.sendMessage(Plugin.prefix + "§cThis shop is not set up yet!");
            else {
                player.openInventory(getShoppingInventory());
                openShops.put(player.getUniqueId(), id);
                updateShopper(player, id);
            }
        } catch (Exception e) {
            player.closeInventory();
            player.sendMessage(Plugin.prefix + "§cAn error occured when attempting to open shop.");
            e.printStackTrace();
        }
    }

    public void updateOwner(Player player, int id) {
        try {
            if (player.getOpenInventory().getTitle().equals("§aShop - Owner")) {
                Inventory menu = player.getOpenInventory().getTopInventory();
                for (int i = 0; i < 27; i++) menu.setItem(i, items.createBlank());
                Shop shop = db.getShop(id);
                Material itemType = shop.item.getType();
                boolean isSelling = shop.isSelling;
                if (itemType.equals(Material.BARRIER)) menu.setItem(4, shop.unselectedItem());
                else menu.setItem(4, new ItemMaker().create("§aSelling: §e" + Plugin.firstLetterCapital(itemType.toString().toLowerCase(), true), itemType, Collections.singletonList("§7Click to remove!")));
                if (isSelling) menu.setItem(10, items.create("§aType: §eSell", Material.CHEST, Collections.singletonList("§7Click to switch to buy!")));
                else menu.setItem(10, items.create("§aType: §eBuy", Material.HOPPER, Collections.singletonList("§7Click to switch to sell!")));
                String ifNeedsSAdd = " Diamond";
                if (shop.price > 1) ifNeedsSAdd = " Diamonds";
                menu.setItem(12, items.create("§aPrice: §b" + shop.price + ifNeedsSAdd + " §9/ §e" + shop.amount,
                        Material.DIAMOND, Arrays.asList("§7Left click to edit price!", "§7Right click to edit amount!")));
                menu.setItem(14, items.create("§aItem Storage", Material.ENDER_CHEST, Collections.singletonList("§7Click to open!")));
                menu.setItem(16, items.create("§bDiamond Storage", Material.DIAMOND_BLOCK, Collections.singletonList("§7Click to open!")));
                menu.setItem(26, items.create("§cRemove", Material.BARRIER, Collections.singletonList("§7Click to remove shop!")));
            }
        } catch (Exception e) {
            player.closeInventory();
            player.sendMessage(Plugin.prefix + "§cAn error occured when attempting to update shop.");
            closeShop(player);
            e.printStackTrace();
        }
    }

    public void updateShopper(Player player, int id) {
        try {
            if (player.getOpenInventory().getTitle().equals("§aShop - Customer")) {
                Inventory menu = player.getOpenInventory().getTopInventory();
                for (int i = 0; i < 27; i++) menu.setItem(i, items.createBlank());
                Shop shop = db.getShop(id);
                Material itemType = shop.item.getType();
                boolean isSelling = shop.isSelling;
                if (!itemType.equals(Material.BARRIER)) {
                    menu.setItem(4, new ItemMaker().create("§e" + Plugin.firstLetterCapital(itemType.toString().toLowerCase(), true), itemType, null));
                    String ifNeedsSAdd = " diamond";
                    if (shop.price > 1) ifNeedsSAdd = " diamonds";
                    if (isSelling) {
                        menu.setItem(11, items.create("§aBuy §b" + shop.amount + " §e" + Plugin.firstLetterCapital(itemType.toString().toLowerCase(), true), Material.HOPPER, Arrays.asList("§7Left click to buy for " + shop.price + ifNeedsSAdd + "!", "§7Middle click to buy all!", "§7Right click for advanced buying!")));
                        menu.setItem(15, items.create("§aStock", Material.ENDER_CHEST, Collections.singletonList("§7Click to view!")));
                    } else {
                        menu.setItem(11, items.create("§aSell §b" + shop.amount + " §e" + Plugin.firstLetterCapital(itemType.toString().toLowerCase(), true), Material.HOPPER, Arrays.asList("§7Left click to sell for " + shop.price + ifNeedsSAdd + "!", "§7Middle click to sell all!", "§7Right click for advanced selling!")));
                        menu.setItem(15, items.create("§bStock", Material.DIAMOND_BLOCK, Collections.singletonList("§7Click to view!")));
                    }
                } else menu.setItem(4, items.create("§cNot currently selling!", Material.BARRIER, Arrays.asList("§7This shop is currently not set up!", "§7Come back later once it is set up!")));
            }
        } catch (Exception e) {
            player.closeInventory();
            player.sendMessage(Plugin.prefix + "§cAn error occured when attmpting to update shop.");
            closeShop(player);
            e.printStackTrace();
        }
    }

    public int getOpenShopId(Player player) {
        return openShops.get(player.getUniqueId());
    }

    public void closeShop(Player player) {
        openShops.remove(player.getUniqueId());
    }

    public void setShopPrice(Player player, int id) {
        player.closeInventory();
        player.sendMessage(Plugin.prefix + "§aType the diamond amount of your shop! Max is 128. You may also type \"cancel.\"");
        db.changingShopPrice.put(player.getUniqueId(), id);
    }

    public void setShopAmount(Player player, int id) {
        player.closeInventory();
        player.sendMessage(Plugin.prefix + "§aType the item amount of your shop! Max is 2304. You may also type \"cancel.\"");
        db.changingShopAmount.put(player.getUniqueId(), id);
    }

    public void openItemStorage(Player player, int id) {
        if (db.editingItemStock.contains(id)) {
          player.sendMessage(Plugin.prefix + "§cSomeone is already updating this stock!");
          return;
        } player.openInventory(getItemInventory());
        if (player.getOpenInventory().getTitle().equals("§aShop - Item Storage")) {
            Inventory menu = player.getOpenInventory().getTopInventory();
            Shop shop = db.getShop(id);
            ItemStack item = shop.item.clone();
            int amount = shop.itemStorage;
            item.setAmount(amount);
            menu.addItem(item);
            openShops.put(player.getUniqueId(), id);
            db.editingItemStock.add(id);
        }
    }

    public void openDiamondStorage(Player player, int id) {
        if (db.editingDiamondStock.contains(id)) {
            player.sendMessage(Plugin.prefix + "§cSomeone is already updating this stock!");
            return;
        } player.openInventory(getDiamondInventory());
        if (player.getOpenInventory().getTitle().equals("§aShop - Diamond Storage")) {
            Inventory menu = player.getOpenInventory().getTopInventory();
            int amount = db.getShop(id).diamondStorage;
            ItemStack item = new ItemMaker().create("", Material.DIAMOND, null, amount);
            menu.addItem(item);
            openShops.put(player.getUniqueId(), id);
            db.editingDiamondStock.add(id);
        }
    }

    public void openCustomerStock(Player player, int id) {
        player.openInventory(getStockInventory());
        if (player.getOpenInventory().getTitle().equals("§aShop - Stock")) {
            Inventory menu = player.getOpenInventory().getTopInventory();
            Shop shop = db.getShop(id);
            if (shop.isSelling) {
                int amount = shop.itemStorage;
                ItemStack item = new ItemMaker().create("", shop.item.getType(), null, amount);
                menu.addItem(item);
            } else {
                int amount = shop.diamondStorage;
                ItemStack item = new ItemMaker().create("", Material.DIAMOND, null, amount);
                menu.addItem(item);
            } openShops.put(player.getUniqueId(), id);
        }
    }

    Inventory createInventory() { return Bukkit.createInventory(this, 27, "§aShop - Owner"); }
    Inventory createItemInventory() { return Bukkit.createInventory(this, 54, "§aShop - Item Storage"); }
    Inventory createDiamondInventory() { return Bukkit.createInventory(this, 54, "§aShop - Diamond Storage"); }
    Inventory createShoppingInventory() { return Bukkit.createInventory(this, 27, "§aShop - Customer"); }
    Inventory createStockInventory() { return Bukkit.createInventory(this, 54, "§aShop - Stock"); }

    @Override
    public Inventory getInventory() { return createInventory(); }
    public Inventory getItemInventory() { return createItemInventory(); }
    public Inventory getDiamondInventory() { return createDiamondInventory(); }
    public Inventory getShoppingInventory() { return createShoppingInventory(); }
    public Inventory getStockInventory() { return createStockInventory(); }
}
