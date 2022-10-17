package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.Main;
import com.grizzly.TheRiseAndFall.gui.TradeMenu;
import com.grizzly.TheRiseAndFall.util.Configs;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CmdTrade implements TabExecutor {

    Main plugin = Main.plugin;

    HashMap<UUID, UUID> currentTradeInvites = new HashMap<>();
    public HashMap<UUID, Integer> runningTasks = new HashMap<>();
    HashMap<Integer, Integer> secondCount = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
        Player player = (Player) sender;
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                if (target == player) {
                    player.sendMessage(Plugin.prefix + "§cYou cannot trade with yourself!");
                    return true;
                } if ((player.getWorld() == target.getWorld() && player.getLocation().distance(target.getLocation()) <= Configs.configs.getConfig().getInt("Trade.Range")) || Configs.configs.getConfig().getInt("Trade.Range") == -1) {
                    if (currentTradeInvites.containsKey(target.getUniqueId()) && currentTradeInvites.get(target.getUniqueId()) == player.getUniqueId()) {
                        if (new TradeMenu(target, false).getReceiver() != null) {
                            player.sendMessage(Plugin.prefix + "§cThat player is already in a trade!");
                            return true;
                        } if (new TradeMenu(player, false).getReceiver() != null) {
                            player.sendMessage(Plugin.prefix + "§cYou are already in a trade!");
                            return true;
                        } new TradeMenu(target, player).open();
                        stopTradeInviteTimer(target.getUniqueId());
                    } else if (!currentTradeInvites.containsKey(player.getUniqueId())) return sendTradePartTwo(player, target, false);
                    else {
                        if (currentTradeInvites.get(player.getUniqueId()).equals(target.getUniqueId()) || ( currentTradeInvites.containsKey(target.getUniqueId())
                                && currentTradeInvites.get(target.getUniqueId()).equals(player.getUniqueId()))) {
                            player.sendMessage(Plugin.prefix + "§cYou already have an outgoing trade request with this person!");
                            return true;
                        } else {
                            Bukkit.getPlayer(currentTradeInvites.get(player.getUniqueId())).sendMessage(Plugin.prefix + "§c" + player.getName() + " has cancelled the trade!");
                            stopTradeInviteTimer(player.getUniqueId());
                            currentTradeInvites.put(player.getUniqueId(), target.getUniqueId());
                            player.sendMessage("§7§oOverwriting previous trade...");
                            return sendTradePartTwo(player, target, true);
                        }
                    }
                } else player.sendMessage(Plugin.prefix + "§cYou have to be within " + Configs.configs.getConfig().getInt("Trade.Range") + " blocks to trade with a player!");
            } else return Plugin.playerOffline(player);
        } else player.sendMessage(Plugin.prefix + "§cUsage: /trade <player>");
        return true;
    }

    boolean sendTradePartTwo(Player player, Player target, boolean hadOutgoing) {
        if (currentTradeInvites.containsValue(player.getUniqueId())) {
            if (!hadOutgoing) {
                Bukkit.getPlayer(currentTradeInvites.get(player.getUniqueId())).sendMessage(Plugin.prefix + "§c" + player.getName() + " has cancelled the trade!");
                stopTradeInviteTimer(currentTradeInvites.get(player.getUniqueId()));
                currentTradeInvites.put(player.getUniqueId(), target.getUniqueId());
                player.sendMessage("§7§oOverwriting previous trade...");
            }
        } currentTradeInvites.put(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(Plugin.prefix + "§aYou have sent a trade request to " + target.getName() + "! They have 30 seconds to accept.");
        target.sendMessage(Plugin.prefix + "§aYou have received a trade request from " + player.getName() + "! Type \"/trade " + player.getName() + "\" to accept.");
        startTradeInviteTimer(player, target);
        return true;
    }

    void startTradeInviteTimer(Player player, Player target) {
        currentTradeInvites.put(player.getUniqueId(), target.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                secondCount.putIfAbsent(getTaskId(), 0);
                if (secondCount.get(getTaskId()) == 0) runningTasks.put(player.getUniqueId(), getTaskId());
                secondCount.put(getTaskId(), secondCount.get(getTaskId()) + 1);
                if (secondCount.get(getTaskId()) == 30) {
                    player.sendMessage(Plugin.prefix + "§cTrade request to " + target.getName() + " has expired.");
                    target.sendMessage(Plugin.prefix + "§cTrade request from " + player.getName() + " has expired.");
                    currentTradeInvites.remove(player.getUniqueId());
                    secondCount.remove(getTaskId());
                    runningTasks.remove(player.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    void stopTradeInviteTimer(UUID player) {
        Bukkit.getScheduler().cancelTask(runningTasks.get(player));
        secondCount.remove(runningTasks.get(player));
        runningTasks.remove(player);
        currentTradeInvites.remove(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) return null;
        return new ArrayList<>();
    }
}
