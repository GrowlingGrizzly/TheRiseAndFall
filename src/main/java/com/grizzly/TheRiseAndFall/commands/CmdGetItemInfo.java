package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CmdGetItemInfo implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
        Player player = (Player) sender;
        if (player.getInventory().getItemInMainHand() != null) {
            ItemStack item = player.getInventory().getItemInMainHand();
            player.sendMessage("§b---------- §dItem Info §b------------");
            player.sendMessage("§3- §eItem Name: §a" + item.getType());
            player.sendMessage("§3- §eAmount: §a" + item.getAmount());
            player.sendMessage("§b---------- §dItem Info §b------------");
        } else player.sendMessage(Plugin.prefix + "§cYou need to hold an item in your hand!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>();
    }
}
