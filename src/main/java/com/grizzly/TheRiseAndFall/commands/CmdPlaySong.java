package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.util.Plugin;
import com.grizzly.TheRiseAndFall.util.Songs;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdPlaySong implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp()) {
            if (!(sender instanceof Player)) if (args.length != 2 && args.length != 3) return invalidSyntax(sender);
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("lose")) {
                    sender.sendMessage(Plugin.prefix + "§aNow playing \"§eLose§a\" to §7" + sender.getName() + "§a.");
                    new Songs().play((Player) sender, "Lose");
                } if (args[0].equalsIgnoreCase("tetris")) {
                    sender.sendMessage(Plugin.prefix + "§aNow playing \"§eTetris§a\" to §7" + sender.getName() + "§a.");
                    new Songs().play((Player) sender, "Tetris");
                }
            } else if (args.length == 2) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    if (args[0].equalsIgnoreCase("lose")) {
                        sender.sendMessage(Plugin.prefix + "§aNow playing \"§eLose§a\" to §7" + target.getName() + "§a.");
                        new Songs().play(target, "Lose");
                    }
                    if (args[0].equalsIgnoreCase("tetris")) {
                        sender.sendMessage(Plugin.prefix + "§aNow playing \"§eTetris§a\" to §7" + target.getName() + "§a.");
                        new Songs().play(target, "Tetris");
                    }
                } else return Plugin.playerOffline(sender);
            } else return invalidSyntax(sender);
        } else return Plugin.noPermission(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) return new ArrayList<>(Arrays.asList("lose", "tetris"));
        if (args.length == 2) return null;
        return new ArrayList<>();
    }

    boolean invalidSyntax(CommandSender sender) {
        sender.sendMessage(Plugin.prefix + "§cUsage: /playsong <song> <player>");
        return true;
    }
}
