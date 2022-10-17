package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.gui.GameMenu;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CmdGames implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
        Player player = (Player) sender;

        if (args.length == 0) new GameMenu().open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return new ArrayList<>();
    }
}