package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.util.Altar;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class CmdAltar implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("materials")) {
                sender.sendMessage("§b----------§d Altar Material List §b------------");
                sender.sendMessage("§3- §eQuartz Slab: §a206");
                sender.sendMessage("§3- §eQuartz Stairs: §a134");
                sender.sendMessage("§3- §eBlock of Quartz: §a115");
                sender.sendMessage("§3- §eBlock of Gold: §a81");
                sender.sendMessage("§3- §eQuartz Pillar: §a64");
                sender.sendMessage("§3- §eBlock of Diamond: §a49");
                sender.sendMessage("§3- §eSea Lantern: §a48");
                sender.sendMessage("§3- §eDeepslate Tile Slab: §a44");
                sender.sendMessage("§3- §eDeepslate Tile Stairs: §a28");
                sender.sendMessage("§3- §eBlock of Emerald: §a25");
                sender.sendMessage("§3- §eCobbled Deepslate Slab: §a20");
                sender.sendMessage("§3- §eBlock of Netherite: §a9");
                sender.sendMessage("§3- §eDeepslate Tile Wall: §a8");
                sender.sendMessage("§3- §eChiseled Deepslate: §a8");
                sender.sendMessage("§3- §eChiseled Quartz Block: §a8");
                sender.sendMessage("§3- §eBeacon: §a1");
                sender.sendMessage("§3- §eAny Glass: §a1");
                sender.sendMessage("§7§oPlace all materials in a chest then close it.");
                sender.sendMessage("§b----------§d Altar Material List §b------------");
            } else if (args[0].equalsIgnoreCase("confirm")) {
                if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
                Player player = (Player) sender;
                if (db.currentAltars.containsKey(player.getUniqueId())) {
                    new Altar(player).getReadyToCreate(true);
                    if (db.readyForAltar.contains(player.getUniqueId())) {
                        if (new Altar(player).isInClaim()) new Altar(player).createAltar();
                        else {
                            player.sendMessage(Plugin.prefix + "§cAll parts of the altar must be in your claim!");
                            new Altar(player).deleteAltarCreation();
                        }
                    } else {
                        player.sendMessage(Plugin.prefix + "§cYou have to have a chest with all altar items to create an altar!");
                        new Altar(player).deleteAltarCreation();
                    }
                } else player.sendMessage(Plugin.prefix + "§cYou have to have a chest with all altar items to create an altar!");
            } else if (args[0].equalsIgnoreCase("cancel")) {
                if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
                Player player = (Player) sender;
                if (db.currentAltars.containsKey(player.getUniqueId())) {
                    new Altar(player).deleteAltarCreation();
                    player.sendMessage(Plugin.prefix + "§aAltar creation cancelled!");
                } else player.sendMessage(Plugin.prefix + "§cYou don't have an altar confirmation to cancel!");
            } else sender.sendMessage(Plugin.prefix + "§cUsage: /altar <materials/confirm/cancel>");
        } else sender.sendMessage(Plugin.prefix + "§cUsage: /altar <materials/confirm/cancel>");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) return new ArrayList<>(Arrays.asList("materials", "confirm", "cancel"));
        return new ArrayList<>();
    }
}
