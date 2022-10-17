package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.Main;
import com.grizzly.TheRiseAndFall.util.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CmdTeam implements TabExecutor {

    Main plugin = Main.plugin;
    HashMap<UUID, UUID> currentInvites = new HashMap<>();
    public HashMap<UUID, Integer> runningTasks = new HashMap<>();
    HashMap<Integer, Integer> secondCount = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
        Player player = (Player) sender;


        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("info")) {
                int teamNum = new Team().getTeamNum(player);
                if (teamNum != -1) return sendInfoMessage(player, teamNum, false);
                else return notInTeam(player, true);
            } else if (args[0].equalsIgnoreCase("disband")) {
                int teamNum = new Team().getTeamNum(player);
                if (teamNum != -1)
                    player.sendMessage(Plugin.prefix + "§cAre you sure you want to disband your team? " +
                            "If you do, all claims will be unclaimed and shops will be removed. Type \"/team disband confirm\" to confirm.");
                else return notInTeam(player, true);
            } else return invalidSyntax(player);
        } if (args.length >= 2 && args[0].equalsIgnoreCase("setname")) {
            if (new Team().isInTeam(player)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]);
                    if (i != args.length-1) sb.append(" ");
                } sb.replace(0, 8, "");
                new Team(player).setName(sb.toString());
                player.sendMessage(Plugin.prefix + "§aTeam name set to §r" + sb.toString().replaceAll("(&([a-f0-9klmnor]))", "§$2"));
            } else return notInTeam(player, true);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite")) {
                if (!new Team().isInTeam(player)) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null) {
                        if (target != player) {
                            if (!currentInvites.containsKey(player.getUniqueId())) {
                                if (!new Team().isInTeam(target)) {
                                    startInviteTimer(player, target);
                                    player.sendMessage(Plugin.prefix + "§aInvitation sent! They have 30 seconds to accept.");
                                    TextComponent msg = new TextComponent(Plugin.prefix + "§aYou have received a team invitation from " + player.getName() + ". ");
                                    TextComponent msg1 = new TextComponent("§2Accept");
                                    TextComponent msg2 = new TextComponent("§cDecline");
                                    msg1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team accept " + player.getName()));
                                    msg2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team deny " + player.getName()));
                                    msg.addExtra(msg1);
                                    msg.addExtra(" §a| ");
                                    msg.addExtra(msg2);
                                    target.spigot().sendMessage(msg);
                                } else player.sendMessage(Plugin.prefix + "§cThat player is already in a team!");
                            } else player.sendMessage(Plugin.prefix + "§cYou already have an outgoing invitation!");
                        } else player.sendMessage(Plugin.prefix + "§cYou cannot invite yourself!");
                        return true;
                    } else return Plugin.playerOffline(player);
                } else return alreadyInTeam(player);
            } else if (args[0].equalsIgnoreCase("accept")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    if (target != player) {
                        if (currentInvites.containsKey(target.getUniqueId()) && currentInvites.get(target.getUniqueId()) == player.getUniqueId()) {
                            if (!new Team().isInTeam(player)) {
                                stopInviteTimer(target.getUniqueId());
                                new Team().createTeam(target, player);
                                player.sendMessage(Plugin.prefix + "§aYou accepted " + target.getName() + "'s invitation!");
                                target.sendMessage(Plugin.prefix + "§a" + player.getName() + " accepted your request!");
                                return true;
                            } else return alreadyInTeam(player);
                        } else player.sendMessage(Plugin.prefix + "§cYou do not currently have an invitation from " + target.getName() + ".");
                    } else player.sendMessage(Plugin.prefix + "§cYou cannot accept or deny yourself!");
                    return true;
                } else return Plugin.playerOffline(player);
            } else if (args[0].equalsIgnoreCase("deny")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    if (target != player) {
                        if (currentInvites.containsKey(target.getUniqueId()) && currentInvites.get(target.getUniqueId()) == player.getUniqueId()) {
                            stopInviteTimer(target.getUniqueId());
                            player.sendMessage(Plugin.prefix + "§cYou denied " + target.getName() + "'s invitation!");
                            target.sendMessage(Plugin.prefix + "§c" + player.getName() + " denied your request!");
                        } else player.sendMessage(Plugin.prefix + "§cYou do not currently have an invitation from " + target.getName() + ".");
                    } else player.sendMessage(Plugin.prefix + "§cYou cannot accept or deny yourself!");
                    return true;
                } else return Plugin.playerOffline(player);
            } else if (args[0].equalsIgnoreCase("info") && player.isOp()) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    int teamNum = new Team().getTeamNum(target);
                    if (teamNum != -1) return sendInfoMessage(player, teamNum, true);
                    else return notInTeam(player, false);
                } else return Plugin.playerOffline(player);
            } else if (args[0].equalsIgnoreCase("disband")) {
                if (args[1].equalsIgnoreCase("confirm")) {
                    int teamNum = new Team().getTeamNum(player);
                    if (teamNum != -1) {
                        Player owner = Bukkit.getPlayer(UUID.fromString(new Team(teamNum).getOwner()));
                        Player coOwner = Bukkit.getPlayer(UUID.fromString(new Team(teamNum).getCoOwner()));
                        if (owner != null) owner.sendMessage(Plugin.prefix + "§cYour team has been disbanded!");
                        else {
                            PlayerData playerData = new PlayerData(new Team(teamNum).getOwner());
                            playerData.set("Awaiting-Team-Disband-Msg", true);
                            playerData.save();
                        } if (coOwner != null) coOwner.sendMessage(Plugin.prefix + "§cYour team has been disbanded!");
                        else {
                            PlayerData playerData = new PlayerData(new Team(teamNum).getCoOwner());
                            playerData.set("Awaiting-Team-Disband-Msg", true);
                            playerData.save();
                        } new Team(owner).removeTeam();
                    } else return notInTeam(player, true);
                    return true;
                } if (player.isOp()) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null) {
                        int teamNum = new Team().getTeamNum(target);
                        if (teamNum != -1) {
                            Player owner = Bukkit.getPlayer(UUID.fromString(new Team(teamNum).getOwner()));
                            Player coOwner = Bukkit.getPlayer(UUID.fromString(new Team(teamNum).getCoOwner()));
                            if (owner != null) owner.sendMessage(Plugin.prefix + "§cYour team has been disbanded!");
                            else {
                                PlayerData playerData = new PlayerData(new Team(teamNum).getOwner());
                                playerData.set("Awaiting-Team-Disband-Msg", true);
                                playerData.save();
                            } if (coOwner != null) coOwner.sendMessage(Plugin.prefix + "§cYour team has been disbanded!");
                            else {
                                PlayerData playerData = new PlayerData(new Team(teamNum).getCoOwner());
                                playerData.set("Awaiting-Team-Disband-Msg", true);
                                playerData.save();
                            } new Team(owner).removeTeam();
                        } else player.sendMessage(Plugin.prefix + "§c" + target.getName() + " is not in a team.");
                    } else return Plugin.playerOffline(player);
                } else return invalidSyntax(player);
            } else return invalidSyntax(player);
        } else return invalidSyntax(player);
        return true;
    }

    boolean alreadyInTeam(Player player) {
        player.sendMessage(Plugin.prefix + "§cYou are already in a team!");
        return true;
    }

    boolean notInTeam(Player player, boolean self) {
        if (self) player.sendMessage(Plugin.prefix + "§cYou are not in a team!");
        else player.sendMessage(Plugin.prefix + "§cThat player is not in a team!");
        return true;
    }

    boolean invalidSyntax(Player player) {
        player.sendMessage(Plugin.prefix + "§cUsage: /team <info/invite/accept/deny/setname>");
        return true;
    }

    boolean sendInfoMessage(Player player, int teamNum, boolean other) {
        Team team = new Team(teamNum);
        if (other) player.sendMessage("§b---------- §dTeam " + teamNum + " §b------------");
        else {
            player.sendMessage("§b---------- " + team.getName(player.getUniqueId().toString()) + " §b------------");
            player.sendMessage("§3- §eName: §a" + team.getName(player.getUniqueId().toString()));
        } player.sendMessage("§3- §eOwner: §a" + team.getOwnerFormatted());
        player.sendMessage("§3- §eCo-Owner: §a" + team.getCoOwnerFormatted());
        player.sendMessage("§3- §eClaims Owned: §a" + new Claim().amountOwned(player));
        if (Configs.configs.getConfig().getBoolean("Lives.Enabled")) player.sendMessage("§3- §eLives Left: §a" + team.getLivesLeft());
        player.sendMessage("§3- §eLand Left: §a" + team.getLandLeft());
        if (other) player.sendMessage("§b---------- §dTeam " + teamNum + " §b------------");
        else player.sendMessage("§b---------- " + team.getName(player.getUniqueId().toString()) + " §b------------");
        return true;
    }

    void startInviteTimer(Player player, Player target) {
        currentInvites.put(player.getUniqueId(), target.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                secondCount.putIfAbsent(getTaskId(), 0);
                if (secondCount.get(getTaskId()) == 0) runningTasks.put(player.getUniqueId(), getTaskId());
                secondCount.put(getTaskId(), secondCount.get(getTaskId()) + 1);
                if (secondCount.get(getTaskId()) == 30) {
                    player.sendMessage(Plugin.prefix + "§cInvitation to " + target.getName() + " has expired.");
                    target.sendMessage(Plugin.prefix + "§cInvitation from " + player.getName() + " has expired.");
                    currentInvites.remove(player.getUniqueId());
                    secondCount.remove(getTaskId());
                    runningTasks.remove(player.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    void stopInviteTimer(UUID player) {
        Bukkit.getScheduler().cancelTask(runningTasks.get(player));
        secondCount.remove(runningTasks.get(player));
        runningTasks.remove(player);
        currentInvites.remove(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) return new ArrayList<>(Arrays.asList("info", "invite", "disband", "setname"));
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("accept") ||
                    args[0].equalsIgnoreCase("deny")) return null;
            if (args[0].equalsIgnoreCase("disband")) return new ArrayList<>(Collections.singletonList("confirm"));
            else if (sender.isOp()) {
                if (args[0].equalsIgnoreCase("info")) return null;
                if (args[0].equalsIgnoreCase("disband")) {
                    ArrayList<String> list = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) list.add(player.getName());
                    list.add("confirm");
                    return list;
                }
            }
        } return new ArrayList<>();
    }
}
