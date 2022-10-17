package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.util.Configs;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdHelp implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender player, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "claims": return helpMenu(player, "Claims", new String[]{
                            "In order to make a claim, you must first",
                            "select your poisitions with /claim pos1",
                            "and /claim pos2. You will then see the border",
                            "of the land you are going to claim in red",
                            "particles. To confirm the claim, do /claim",
                            "confirm, or if you would like to cancel the",
                            "claim, do /claim cancel."});
                case "teams": return helpMenu(player, "Teams", new String[]{
                        "In order to make a team, invite a player",
                        "with \"/team invite <player>.\" To accept",
                        "a team invite, click on the accept button",
                        "on the invite. You can also click the deny",
                        "button to deny the request. There is a max",
                        "of 2 players per team."});
                case "commissions": return helpMenu(player, "Commmissions", new String[]{
                        "In order to see your commissions, type",
                        "/commissions. Hover over the books to see",
                        "your current commissions. Click on the",
                        "commission to submit items and claim your",
                        "reward. If you want to reroll commissions",
                        "you have not put items into, click on the",
                        "paper, and for the cost of 1 diamond, your",
                        "commissions will be rerolled."});
                case "trades":
                    ArrayList<String> lines = new ArrayList<>(Collections.singletonList("You can trade with a player from anywhere."));
                    if (Configs.configs.getConfig().getInt("Trade.Range") != -1) lines = new ArrayList<>(Arrays.asList(
                            "In order to trade with someone, you must be", "within " + Configs.configs.getConfig().getInt("Trade.Range") + " blocks of the other player."));
                    lines.add("To trade with them, type /trade <player>. They");
                    lines.addAll(List.of(new String[]{
                            "To trade with them, type /trade <player>. They",
                            "then type it with your name, and the trade menu",
                            "opens. You can then then put items in, then click",
                            "the red glass at the bottom left to confirm and trade."}));
                    return helpMenu(player, "Trades", lines.toArray(new String[0]));
                /*altar code
                case "altars": return helpMenu(player, "Altars", new String[]{
                        "An altar lets you teleport to spawn and back",
                        "to your base. You can have 1 per team.",
                        "In order to see how to make one, put all",
                        "required items into a chest. To view these,",
                        "type \"/altar materials.\" Once you close the",
                        "chest with all the materials inside of it,",
                        "type \"/altar confirm\" to confirm the build.",
                        "to remove the altar, break any block and all",
                        "materials will go back into a chest. You can",
                        "use your own altar and the church's altar."});*/
                default: return mainMenu(player);
            }
        } else return mainMenu(player);
    }

    boolean helpMenu(CommandSender player, String name, String[] lines) {
        player.sendMessage("§b----------§d Help - " + name + " §b------------");
        ArrayList<String> list = new ArrayList<>(List.of(lines));
        player.sendMessage("§a" + list.get(0));
        list.remove(0);
        for (String string : list) player.sendMessage("§a " + string);
        player.sendMessage("§b----------§d Help - " + name + " §b------------");
        return true;
    }
    
    boolean mainMenu(CommandSender player) {
        player.sendMessage("§b----------§d Help - Main Menu §b------------");
        Plugin.sendInteractiveMessage(player, "§3- §eClaims: §aGet information on how to make claims.", "/help claims");
        Plugin.sendInteractiveMessage(player, "§3- §eTeams: §aGet information on how to make teams.", "/help teams");
        Plugin.sendInteractiveMessage(player, "§3- §eCommissions: §aGet information on how to do commissions.", "/help commissions");
        Plugin.sendInteractiveMessage(player, "§3- §eTrades: §aGet information on how to trade with players.", "/help trades");
        //Plugin.sendInteractiveMessage(player, "§3- §eAltars: §aGet information on how to make an altar.", "/help altars");
        player.sendMessage("§7§oClick on one of the lines to view the help for the command.");
        player.sendMessage("§b----------§d Help - Main Menu §b------------");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        //if (args.length == 1) return new ArrayList<>(Arrays.asList("claims", "teams", "commissions", "trades", "altars"));
        return new ArrayList<>();
    }
}
