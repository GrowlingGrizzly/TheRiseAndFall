package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.gui.CommissionMenu;
import com.grizzly.TheRiseAndFall.util.Commission;
import com.grizzly.TheRiseAndFall.util.Configs;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdCommissions implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
        Player player = (Player) sender;
        if (args.length == 0) new CommissionMenu(player).open();
        else if (args.length == 1) return syntax(player);
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    new Commission(target).createDefaults(true);
                    player.sendMessage(Plugin.prefix + "§aSuccessfully reset commissions for §6" + target.getName() + "§a!");
                    target.sendMessage(Plugin.prefix + "§aYour commissions have been reset by §6" + player.getName() + "§a!");
                } else return Plugin.playerOffline(sender);
            } else if (args[0].equalsIgnoreCase("replace")) return specificSyntax(player, false);
            else return syntax(player);
        } else if (args.length > 2) {
            if (player.isOp()) {
                if (args[0].equalsIgnoreCase("reset")) return specificSyntax(player, true);
                else if (args[0].equalsIgnoreCase("replace")) {
                    if (args[1].equalsIgnoreCase("0") || args[1].equalsIgnoreCase("1")) {
                        if (args.length < 5) {
                            Player target = Bukkit.getPlayer(args[2]);
                            if (player != null) {
                                if (args.length == 3) {
                                    if (args[1].equalsIgnoreCase("0")) new Commission(target).createSingle(0, null);
                                    else new Commission(target).createSingle(1, null);
                                    player.sendMessage(Plugin.prefix + "§aSuccessfully reset commission §e" + args[1] + "§a for §6" + target.getName() + "§a!");
                                    target.sendMessage(Plugin.prefix + "§aCommission §e" + args[1] + " §ahas been reset by §6" + player.getName() + "§a!");
                                } else if (args.length == 4) {
                                    if (new Commission(target).isValidId(args[3])) {
                                        if (args[1].equalsIgnoreCase("0"))
                                            new Commission(target).createSingle(0, args[3].toLowerCase());
                                        else new Commission(target).createSingle(1, args[3].toLowerCase());
                                        player.sendMessage(Plugin.prefix + "§aSuccessfully set commission §e" + args[1] + "§a for §6" + target.getName() + "§a to §3" + args[3].toLowerCase() + "§a!");
                                        target.sendMessage(Plugin.prefix + "§aCommission §e" + args[1] + " §ahas been set to §3" + args[3].toLowerCase() + "§a by §6" + player.getName() + "§a!");
                                    } else
                                        player.sendMessage(Plugin.prefix + "§cYou must enter a valid commission id!");
                                } else return syntax(player);
                            } else return specificSyntax(player, false);
                        } else return specificSyntax(player, false);
                    } else return specificSyntax(player, false);
                } else return syntax(player);
            } else return syntax(player);
        } return true;
    }

    boolean syntax(Player player) {
        player.sendMessage(Plugin.prefix + "§cUsage: /commissions <reset, replace> <player/num> <player (if applicable)> <id (if applicable)>");
        return true;
    }

    boolean specificSyntax(Player player, boolean isReset) {
        if (isReset) player.sendMessage(Plugin.prefix + "§cUsage: /commissions reset <player>");
        else player.sendMessage(Plugin.prefix + "§cUsage: /commissions replace <0/1> <player> <replaceWithID (if applicable)>");
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("theriseandfall.commissionmenu")) {
            if (args.length == 1) return new ArrayList<>(Arrays.asList("reset", "replace"));
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("reset")) {
                    return null;
                } if (args[0].equalsIgnoreCase("replace")) return new ArrayList<>(Arrays.asList("0", "1"));
            } if (args.length == 3 && args[0].equalsIgnoreCase("replace") && (args[1].equalsIgnoreCase("0") || args[1].equalsIgnoreCase("1"))) return null;
            if (args.length == 4) return new ArrayList<>(Configs.configs.getCommissions().getConfigurationSection("Commissions").getKeys(false));
        } return new ArrayList<>();
    }
}
