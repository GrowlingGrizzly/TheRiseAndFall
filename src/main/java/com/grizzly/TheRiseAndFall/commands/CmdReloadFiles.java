package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.util.Configs;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdReloadFiles implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("theriseandfall.admin.reloadfiles")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("config")) {
                    Configs.configs.reloadDefaultConfig();
                    sender.sendMessage(Plugin.prefix + "§aConfiguration file reloaded!");
                } else if (args[0].equalsIgnoreCase("claims")) {
                    Configs.configs.reloadClaims();
                    sender.sendMessage(Plugin.prefix + "§aClaims file reloaded!");
                } else if (args[0].equalsIgnoreCase("teams")) {
                    Configs.configs.reloadTeams();
                    sender.sendMessage(Plugin.prefix + "§aTeams file reloaded!");
                } else if (args[0].equalsIgnoreCase("commissions")) {
                    Configs.configs.reloadCommissions();
                    sender.sendMessage(Plugin.prefix + "§aCommissions file reloaded!");
                } else if (args[0].equalsIgnoreCase("shops")) {
                    Configs.configs.reloadShops();
                    sender.sendMessage(Plugin.prefix + "§aShops file reloaded!");
                } else if (args[0].equalsIgnoreCase("altars")) {
                    Configs.configs.reloadAltars();
                    sender.sendMessage(Plugin.prefix + "§aAltars file reloaded!");
                } else return invalidSyntax(sender);
            } else return invalidSyntax(sender);
        } else return Plugin.noPermission(sender);
        return true;
    }


    boolean invalidSyntax(CommandSender sender) {
        sender.sendMessage(Plugin.prefix + "§cUsage: /reloadfiles <config/claims/teams/commissions>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("theriseandfall.admin.reloadfiles") && args.length == 1)
            return new ArrayList<>(Arrays.asList("config", "claims", "teams", "commissions", "shops", "altars"));
        return new ArrayList<>();
    }
}
