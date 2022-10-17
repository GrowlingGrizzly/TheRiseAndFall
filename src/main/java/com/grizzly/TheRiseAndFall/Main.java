package com.grizzly.TheRiseAndFall;

import com.grizzly.TheRiseAndFall.commands.*;
import com.grizzly.TheRiseAndFall.events.*;
import com.grizzly.TheRiseAndFall.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.Collection;

import static com.grizzly.TheRiseAndFall.util.Database.db;


public class Main extends JavaPlugin {

    public static Main plugin;

    @Override
    public void onEnable() {

        plugin = this;

        new Configs(this).startup();

        if (Configs.configs.getConfig().getBoolean("Discord-Integration.Enabled")) new DiscordIntegration().startup();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new CommissionEvents(), this);
        pm.registerEvents(new PlayerEvents(), this);
        pm.registerEvents(new BlockEvents(), this);
        pm.registerEvents(new TradeEvents(), this);
        pm.registerEvents(new AltarEvents(), this);
        pm.registerEvents(new ShopEvents(), this);
        pm.registerEvents(new GameEvents(), this);
        pm.registerEvents(new MainEvents(), this);

        getCommand("reloadfiles").setExecutor(new CmdReloadFiles());
        getCommand("getiteminfo").setExecutor(new CmdGetItemInfo());
        getCommand("commissions").setExecutor(new CmdCommissions());
        getCommand("setitemname").setExecutor(new CmdSetItemName());
        getCommand("playerinfo").setExecutor(new CmdPlayerInfo());
        getCommand("playsong").setExecutor(new CmdPlaySong());
        getCommand("children").setExecutor(new CmdChildren());
        getCommand("admin").setExecutor(new CmdAdmin());
        getCommand("trade").setExecutor(new CmdTrade());
        //getCommand("altar").setExecutor(new CmdAltar());
        getCommand("claim").setExecutor(new CmdClaim());
        getCommand("games").setExecutor(new CmdGames());
        getCommand("help").setExecutor(new CmdHelp());
        getCommand("team").setExecutor(new CmdTeam());

        for (String id : Configs.configs.getShops().getKeys(false)) new Shop(null, Integer.parseInt(id)).reload();

        Plugin.log.info(Plugin.prefix + "§aPlugin has been enabled!");

        Bukkit.getScheduler().runTask(plugin, () -> getServer().getOnlinePlayers().forEach(db::loadInventoryIfBackedUp));
        Bukkit.getScheduler().runTaskLater(plugin, () -> new Shop(null, -1).spawnItems(true), 40);

        db.cacheClaims();

        DiscordWebhook.sendWebhookIfEnabled("Server Status", "Server is online!", db.formattedTime(), new Color(0, 204, 0));
    }

    @Override
    public void onDisable() {

        for (Player player : Bukkit.getOnlinePlayers()) for (String title : new String[]{"§9§lGames", "§2§lSnake", "§6§lSelect your speed", "§aShop - "}) {
            if (player.getOpenInventory().getTitle().contains(title)) player.closeInventory();
        }

        Collection<Entity> entities = Bukkit.getWorld("world").getEntities();
        for (Entity entity : entities) {
            if (entity.getCustomName() != null && entity.getCustomName().contains("§edisplayStand")) entity.remove();
            if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
                Item item = (Item) entity;
                if (item.getItemStack().getItemMeta().getDisplayName().contains("§eshopItem §c")) entity.remove();
            }
        } plugin = null;

        new DiscordIntegration().shutDown();

        Plugin.log.info(Plugin.prefix + "§cPlugin has been disabled!");

        DiscordWebhook.sendWebhookIfEnabled("Server Status", "Server is offline!", db.formattedTime(), new Color(204, 0, 0));

    }



}
