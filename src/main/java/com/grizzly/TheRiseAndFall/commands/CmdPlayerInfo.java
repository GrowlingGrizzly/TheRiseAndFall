package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.util.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CmdPlayerInfo implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Plugin.prefix + "§cUsage: /playerinfo <player>");
                return true;
            } return sendPlayerInfo(sender, (Player) sender, true);
        } if (sender.isOp()) {
            if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) return sendPlayerInfo(sender, target, false);
                else return Plugin.playerOffline(sender);
            } else sender.sendMessage(Plugin.prefix + "§cUsage: /playerinfo <player>");
        } else return Plugin.noPermission(sender);
        return true;
    }

    boolean sendPlayerInfo(CommandSender sender, Player target, boolean self) {
        if (self) sender.sendMessage("§b---------- §d Your Info §b------------");
        else sender.sendMessage("§b---------- §d" + target.getName() + "'s Info §b------------");
        if (new Team().isInTeam(target)) {
            Team team = new Team(target);
            sender.sendMessage("§3- §eIn Team: §aYes");
            if (!self) sender.sendMessage("   §3- §eTeam Number: §a" + new Team().getTeamNum(target));
            sender.sendMessage("   §3- §eName: §a" + team.getName(target.getUniqueId().toString()));
            sender.sendMessage("   §3- §eOwner: §a" + team.getOwnerFormatted());
            sender.sendMessage("   §3- §eCo-Owner: §a" + team.getCoOwnerFormatted());
            sender.sendMessage("   §3- §eClaims Owned: §a" + new Claim().amountOwned(target));
            if (Configs.configs.getConfig().getBoolean("Lives.Enabled")) sender.sendMessage("   §3- §eLives Left: §a" + team.getLivesLeft());
            sender.sendMessage("   §3- §eLand Left: §a" + team.getLandLeft());
        } else {
            PlayerData getUserData = new PlayerData(target.getUniqueId());
            sender.sendMessage("§3- §eIn Team: §cNo");
            sender.sendMessage("§3- §eClaims Owned: §a" + new Claim().amountOwned(target));
            if (Configs.configs.getConfig().getBoolean("Lives.Enabled")) sender.sendMessage("§3- §eLives Left: §a" + getUserData.getString("Lives-Left"));
            sender.sendMessage("§3- §eLand Left: §a" + getUserData.getString("Land-Left"));
        } sender.sendMessage("§3- §eCommissions:");
        sender.sendMessage("   §3- §eAmount Completed: §a" + new Commission(target).getAmountCompleted());
        if (!self) {
            sendCommissionText(sender, target, 0);
            sendCommissionText(sender, target, 1);
            sender.sendMessage("§b---------- §d" + target.getName() + "'s Info §b------------");
        } else sender.sendMessage("§b---------- §d Your Info §b------------");
        return true;
    }

    void sendCommissionText(CommandSender sender, Player target, Integer commNum) {
        Commission comm = new Commission(target, commNum);
        sender.sendMessage("   §3- §eCommission " + (commNum + 1) + " (" + comm.getName() + "):");
        sender.sendMessage("      §3- §eNeeded: §a" + comm.getAmount() + " " + Plugin.firstLetterCapital(comm.getItem().toString(), true));
        sender.sendMessage("      §3- §eProgress: §a" + (comm.getProgress()*100) / comm.getAmount() + "% (" + comm.getProgress() + "/" + comm.getAmount() + ")");
        if (comm.getRewardType().equals("ITEM"))
            sender.sendMessage("      §3- §eReward: §a" + comm.getRewardAmount() + " " + Plugin.firstLetterCapital(comm.getRewardItem().toString(), true));
        else sender.sendMessage("      §3- §eReward: §a" + comm.getRewardAmount() + " Land Claim");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && sender.isOp()) return null;
        return new ArrayList<>();
    }
}
