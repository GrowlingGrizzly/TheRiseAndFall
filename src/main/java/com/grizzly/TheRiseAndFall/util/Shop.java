package com.grizzly.TheRiseAndFall.util;

import com.grizzly.TheRiseAndFall.Main;
import com.grizzly.TheRiseAndFall.gui.ShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class Shop {

    public int diamondStorage;
    public boolean isSelling;
    public Location location;
    public int itemStorage;
    public ItemStack item;
    CommandSender player;
    public int amount;
    public int price;
    String coOwner;
    String owner;
    int id;


    public Shop(Player player, int id) {
        if (id == -1 || !Configs.configs.getShops().isConfigurationSection(String.valueOf(id))) {
            if (player != null) owner = player.getUniqueId().toString();
            this.id = createId();
            isSelling = true;
            item = null;
            location = null;
            itemStorage = 0;
            diamondStorage = 0;
            price = 1;
            amount = 1;
            this.player = player;
        } else {
            this.id = id;
            this.player = player;
            reload();
        }
    }

    public Shop(CommandSender sender, int id) {
        this.id = id;
        player = sender;
        reload();
    }

    int createId() {
        int num = 0;
        while (Configs.configs.getShops().isConfigurationSection(String.valueOf(num))) num++;
        return num;
    }

    public ItemStack unselectedItem() {
        return new ItemMaker().create("§cNo Item Selected", Material.BARRIER, Arrays.asList("§7Click an item in your inventory to use in shop!", "§7Note: Custom names & enchants are unavailable!"));
    }

    void save() {
        Configs config = Configs.configs;
        config.getShops().set(id + ".Storage.Diamond", diamondStorage);
        config.getShops().set(id + ".Storage.Item", itemStorage);
        config.getShops().set(id + ".IsSelling", isSelling);
        config.getShops().set(id + ".Location", location);
        config.getShops().set(id + ".CoOwner", coOwner);
        config.getShops().set(id + ".Amount", amount);
        config.getShops().set(id + ".Price", price);
        config.getShops().set(id + ".Owner", owner);
        config.getShops().set(id + ".Item", item);
        config.saveShops();
        reload();
    }

    public void reload() {
        Configs config = Configs.configs;
        diamondStorage = config.getShops().getInt(id + ".Storage.Diamond");
        itemStorage = config.getShops().getInt(id + ".Storage.Item");
        isSelling = config.getShops().getBoolean(id + ".IsSelling");
        location = config.getShops().getLocation(id + ".Location");
        coOwner = config.getShops().getString(id + ".CoOwner");
        item = config.getShops().getItemStack(id + ".Item");
        owner = config.getShops().getString(id + ".Owner");
        amount = config.getShops().getInt(id + ".Amount");
        price = config.getShops().getInt(id + ".Price");
        db.setShop(id, this);
    }


    public void startCreation(Inventory chest) {
        Chest chestData = (Chest) chest.getLocation().getBlock().getBlockData().clone();
        chest.getLocation().getBlock().setType(Material.STONE_STAIRS);
        Stairs stairData = (Stairs) chest.getLocation().getBlock().getBlockData().clone();
        BlockFace facing = BlockFace.NORTH;
        switch (chestData.getFacing()) {
            case EAST: facing = BlockFace.WEST; break;
            case WEST: facing = BlockFace.EAST; break;
            case NORTH: facing = BlockFace.SOUTH; break;
        } stairData.setFacing(facing);
        stairData.setHalf(Bisected.Half.TOP);
        chest.getLocation().getBlock().setBlockData(stairData);
        ArmorStand displayCase = (ArmorStand) chest.getLocation().getWorld().spawnEntity(chest.getLocation().add(0.5D, -0.3D, 0.5D), EntityType.ARMOR_STAND);
        displayCase.setInvisible(true);
        displayCase.setInvulnerable(true);
        displayCase.setGravity(false);
        displayCase.getEquipment().setHelmet(new ItemMaker().create("§edisplayCase", Material.GLASS, null));
        displayCase.setCustomName("§edisplayStand §c" + id);
        player.sendMessage(Plugin.prefix + "§aShop creation started! Right click the display case to configure.");
        location = chest.getLocation();
        item = unselectedItem();
        if (!new Team().isInTeam((Player) player)) {
            owner = ((Player) player).getUniqueId().toString();
            coOwner = "UNSET";
        } if (new Team().isInTeam((Player) player)) {
            owner = new Team((Player) player).getOwner();
            coOwner = new Team((Player) player).getCoOwner();
        } save();
        updateDisplay(false);
    }

    public void spawnItems(boolean startLoop) {
        Main plugin = Main.plugin;
        if (startLoop) {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                for (Integer id : db.getShopIds()) new Shop(null, id).updateDisplay(false);
            }, 0, 1200);
        } else for (Integer id : db.getShopIds()) summonIndividualShopItems(id);
    }

    void summonIndividualShopItems(int id) {
        Shop shop = db.getShop(id);
        ArmorStand displayCase = (ArmorStand) shop.location.getWorld().
                spawnEntity(shop.location.clone().add(0.5D, -0.3D, 0.5D), EntityType.ARMOR_STAND);
        displayCase.setInvisible(true);
        displayCase.setInvulnerable(true);
        displayCase.setGravity(false);
        displayCase.getEquipment().setHelmet(new ItemMaker().create("§edisplayCase", Material.GLASS, null));
        displayCase.setCustomName("§edisplayStand §c" + id);

        ItemStack item = shop.item.clone();
        item.setAmount(1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eshopItem §c" + id);
        item.setItemMeta(meta);
        Location loc = shop.location.getBlock().getLocation().clone();
        Item item1 = loc.getWorld().dropItem(loc.add(0.5D, 1.1D, 0.5D), item);
        item1.setVelocity(new Vector(0, 0, 0));
        item1.setGravity(false);
        item1.setInvulnerable(true);
        if (item.getType().equals(Material.BARRIER)) item1.setCustomName("§a$ §cShop waiting configuration. §a$");
        else {
            String ifNeedsSAdd = " Diamond";
            if (shop.price > 1) ifNeedsSAdd = " Diamonds";
            if (!shop.isSelling)
                item1.setCustomName("§a$ Buying: §e" + Plugin.firstLetterCapital(shop.item.getType().toString().toLowerCase(), true) + " §a(§b" + shop.price + ifNeedsSAdd + " §c/ §e" + shop.amount + "§a)");
            else item1.setCustomName("§a$ Selling: §e" + Plugin.firstLetterCapital(shop.item.getType().toString().toLowerCase(), true) + " §a(§b" + shop.price + ifNeedsSAdd + " §c/ §e" + shop.amount + "§a)");
        } item1.setCustomNameVisible(true);
    }

    public void updateDisplay(boolean forceNeg1) {
        Collection<Entity> entities = Bukkit.getWorld("world").getEntities();
        if (forceNeg1) id = -1;
        if (id == -1) {
            for (Entity entity : entities) {
                if (entity.getCustomName() != null && entity.getCustomName().contains("§edisplayStand")) entity.remove();
                if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
                    Item item = (Item) entity;
                    if (item.getItemStack().getItemMeta().getDisplayName().contains("§eshopItem §c")) entity.remove();
                }
            } spawnItems(false);
        } else {
            for (Entity entity : entities) {
                if (entity.getCustomName() != null && entity.getCustomName().contains("§edisplayStand §c" + id)) entity.remove();
                if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
                    Item item = (Item) entity;
                    if (item.getItemStack().getItemMeta().getDisplayName().equals("§eshopItem §c" + id)) entity.remove();
                }
            } summonIndividualShopItems(id);
        }
    }

    public void setShopItem(Player player, int slot) {
        if (player.getInventory().getItem(slot) != null) {
            for (Material mat : new Material[]{Material.POTION, Material.LINGERING_POTION, Material.SPLASH_POTION,
                    Material.BARRIER, Material.ENCHANTED_BOOK, Material.WRITTEN_BOOK, Material.WRITABLE_BOOK,
                    Material.DIAMOND, Material.DIAMOND_BLOCK, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE}) {
                if (player.getInventory().getItem(slot).getType().equals(mat)) {
                    player.sendMessage(Plugin.prefix + "§cThat item cannot be put in a shop.");
                    return;
                }
            } item = new ItemMaker().create("", player.getInventory().getItem(slot).getType(), null);
            save();
            new ShopGUI().updateOwner(player, id);
            updateDisplay(false);
        }
    }

    public void removeShopItem(Player player) {
        if (itemStorage == 0) {
            item = unselectedItem();
            save();
            new ShopGUI().updateOwner(player, id);
            updateDisplay(false);
        } else player.sendMessage(Plugin.prefix + "§cItem storage must be empty to remove the item!");
    }

    public void changeToTeam(Player owner, Player coOwner) {
        Configs.configs.getShops().set(id + ".Owner", owner.getUniqueId().toString());
        Configs.configs.getShops().set(id + ".CoOwner", coOwner.getUniqueId().toString());
        Configs.configs.saveShops();
    }

    public void swapSelling() {
        isSelling = !isSelling;
        save();
    }

    public void setStorage(boolean isItemStorage, int amount) {
        if (isItemStorage) itemStorage = amount;
        else diamondStorage = amount;
        save();
    }

    public void open() {
        if (player instanceof Player) {
            Player player = (Player) this.player;
            if (player.getUniqueId().equals(UUID.fromString(owner))) new ShopGUI().openOwner(player, id);
            else if (!coOwner.equals("UNSET")) {
                if (player.getUniqueId().equals(UUID.fromString(coOwner))) new ShopGUI().openOwner(player, id);
                else new ShopGUI().openShopper(player, id);
            } else new ShopGUI().openShopper(player, id);
        }
    }

    public void setPrice(int amount) {
        price = amount;
        save();
    }

    public void setAmount(int amount) {
        this.amount = amount;
        save();
    }

    public void remove(boolean force) {
        Configs config = Configs.configs;
        if ((itemStorage == 0 && diamondStorage == 0) || force) {
            Collection<Entity> nearbyStands = location.getWorld().getNearbyEntities(location, 2, 2, 2, (entity) -> entity.getType() == EntityType.ARMOR_STAND);
            Collection<Entity> nearbyItems = location.getWorld().getNearbyEntities(location, 2, 2, 2, (entity) -> entity.getType() == EntityType.DROPPED_ITEM);
            for (Entity stand : nearbyStands) if (stand.getCustomName().contains("§c" + id)) stand.remove();
            for (Entity item : nearbyItems) {
                Item itemStack = (Item) item;
                if (itemStack.getItemStack().getItemMeta().getDisplayName().contains("§eshopItem §c")) item.remove();
            } Stairs stairData = (Stairs) location.getBlock().getBlockData().clone();
            location.getBlock().setType(Material.CHEST);
           Chest chestData = (Chest) location.getBlock().getBlockData().clone();
            BlockFace facing = BlockFace.NORTH;
            switch (stairData.getFacing()) {
                case EAST: facing = BlockFace.WEST; break;
                case WEST: facing = BlockFace.EAST; break;
                case NORTH: facing = BlockFace.SOUTH; break;
            } chestData.setFacing(facing);
            location.getBlock().setBlockData(chestData);
            config.getShops().set(String.valueOf(id), null);
            config.saveShops();
            if (player instanceof Player) ((Player) player).closeInventory();
            db.removeShop(id);
            if (player != null) player.sendMessage(Plugin.prefix + "§aShop removed!");
        } else player.sendMessage(Plugin.prefix + "§cStorages must be empty to remove shop!");
    }

}
