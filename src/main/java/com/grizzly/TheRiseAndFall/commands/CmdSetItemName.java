package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CmdSetItemName implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
        Player player = (Player) sender;
        if (player.isOp()) {
            if (args.length > 0) {
                if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    ItemStack item = player.getInventory().getItemInMainHand().clone();
                    ItemMeta meta = item.getItemMeta();
                    StringBuilder sb = new StringBuilder();
                    for (String word : args) {
                        sb.append(word);
                        sb.append(" ");
                    } sb.deleteCharAt(sb.length()-1);
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', sb.toString()));
                    player.getInventory().getItemInMainHand().setItemMeta(meta);
                    player.sendMessage(Plugin.prefix + "§aItem name set to " + ChatColor.translateAlternateColorCodes('&', sb.toString()));
                } else player.sendMessage(Plugin.prefix + "§cYou must be holding an item in your hand!");
            } else player.sendMessage(Plugin.prefix + "§cUsage: /setitemname <name>");
        } else return Plugin.noPermission(sender);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return new ArrayList<>();
    }
}
