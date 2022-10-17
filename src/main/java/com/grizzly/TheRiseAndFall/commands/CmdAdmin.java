package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.gui.ShopGUI;
import com.grizzly.TheRiseAndFall.util.PlayerData;
import com.grizzly.TheRiseAndFall.util.Plugin;
import com.grizzly.TheRiseAndFall.util.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class CmdAdmin implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp()) {
            if (args.length > 0) {
                int id = -1;
                try { id = Integer.parseInt(args[1]); } catch (Exception ignored) {}
                if (args[0].equalsIgnoreCase("removeshop")) {
                    if (args.length == 2 || args.length == 3) {
                        if (db.getShopIds().contains(id)) {
                            boolean force = args.length == 3 && args[2].equalsIgnoreCase("--force");
                            new Shop(sender, Integer.parseInt(args[1])).remove(force);
                        } else sender.sendMessage(Plugin.prefix + "§cInvalid shop id.");
                    } else sender.sendMessage(Plugin.prefix + "§cUsage: /admin removeshop <id>");
                } else if (args[0].equalsIgnoreCase("editshop")) {
                    if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
                    Player player = (Player) sender;
                    if (args.length == 2) {
                        if (db.getShopIds().contains(id)) new ShopGUI().openOwner(player, Integer.parseInt(args[1]));
                        else sender.sendMessage(Plugin.prefix + "§cInvalid shop id.");
                    } else player.sendMessage(Plugin.prefix + "§cUsage: /admin editshop <id>");
                } else if (args[0].equalsIgnoreCase("reloadshopitem")) {
                    if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("all")) {
                            new Shop(null, -1).updateDisplay(true);
                            sender.sendMessage(Plugin.prefix + "§aAll shop items reloaded!");
                        } else {
                            if (db.getShopIds().contains(id)) {
                                new Shop(null, Integer.parseInt(args[1])).updateDisplay(false);
                                sender.sendMessage(Plugin.prefix + "§aShop §e" + args[1] + "§a's items reloaded!");
                            } else sender.sendMessage(Plugin.prefix + "§cInvalid shop id.");
                        }
                    } else sender.sendMessage(Plugin.prefix + "§cUsage: /admin reloadshopitem <id/all>");
                } else if (args[0].equalsIgnoreCase("ignoreclaims")) {
                    if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
                    PlayerData data = new PlayerData(((Player) sender).getUniqueId().toString());
                    if (!data.isIgnoringClaims()) {
                        sender.sendMessage(Plugin.prefix + "§aNow ignoring claims.");
                        data.setIgnoringClaims(true);
                    } else {
                        sender.sendMessage(Plugin.prefix + "§aNo longer ignoring claims.");
                        data.setIgnoringClaims(false);
                    }
                } else sender.sendMessage(Plugin.prefix + "§cUsage: /admin <removeshop/editshop/reloadshopitem/ignoreclaims> <args>");
            } else sender.sendMessage(Plugin.prefix + "§cUsage: /admin <removeshop/editshop/reloadshopitem/ignoreclaims> <args>");
        } else return Plugin.noPermission(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp()) {
            if (args.length == 1) return new ArrayList<>(Arrays.asList("removeshop", "editshop", "reloadshopitem", "ignoreclaims"));
            if (args[0].equalsIgnoreCase("reloadshopitem") && args.length == 2) return new ArrayList<>(Collections.singletonList("all"));
        } return new ArrayList<>();
    }
}
