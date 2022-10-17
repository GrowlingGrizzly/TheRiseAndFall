package com.grizzly.TheRiseAndFall.commands;

import com.grizzly.TheRiseAndFall.Main;
import com.grizzly.TheRiseAndFall.util.Claim;
import com.grizzly.TheRiseAndFall.util.PlayerData;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CmdClaim implements TabExecutor {

    HashMap<UUID, ArrayList<Integer>> runningTasks = new HashMap<>();
    HashMap<UUID, ArrayList<Integer[]>> outerClaimCoords = new HashMap<>();
    public HashMap<UUID, ArrayList<Integer[]>> overlappingClaimCoords = new HashMap<>();
    HashMap<UUID, Integer[]> pos1 = new HashMap<>();
    HashMap<UUID, Integer[]> pos2 = new HashMap<>();
    HashMap<UUID, Boolean> claimParticles = new HashMap<>();
    HashMap<UUID, Integer> unclaimTimer = new HashMap<>();

    Main plugin = Main.plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return Plugin.playerOnly(sender);
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        runningTasks.putIfAbsent(uuid, new ArrayList<>());
        claimParticles.put(uuid, false);
        if (args.length == 0) return invalidSyntax(player);
        if (args.length != 1 && !(player.isOp() && (args[0].equalsIgnoreCase("getclaimat")) || args[0].equalsIgnoreCase("getclaim"))) return invalidSyntax(player);
        else {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("pos1")) {
                    pos1.put(uuid, new Integer[]{player.getLocation().getBlockX(), player.getLocation().getBlockZ()});
                    player.sendMessage(Plugin.prefix + "§eClaim Position 1 set to " + pos1.get(uuid)[0] + ", " + pos1.get(uuid)[1]);
                    ifReadyStartParticles(player);
                } else if (args[0].equalsIgnoreCase("pos2")) {
                    pos2.put(uuid, new Integer[]{player.getLocation().getBlockX(), player.getLocation().getBlockZ()});
                    player.sendMessage(Plugin.prefix + "§eClaim Position 2 set to " + pos2.get(uuid)[0] + ", " + pos2.get(uuid)[1]);
                    ifReadyStartParticles(player);
                } else if (args[0].equalsIgnoreCase("confirm")) {
                    if (pos1.get(uuid) == null || pos2.get(uuid) == null)
                        player.sendMessage(Plugin.prefix + "§cSelect both claim positions first!");
                    else {

                        ArrayList<Integer> coordsX = new ArrayList<>(getCoords(pos1.get(uuid)[0], pos2.get(uuid)[0]));
                        ArrayList<Integer> coordsZ = new ArrayList<>(getCoords(pos1.get(uuid)[1], pos2.get(uuid)[1]));

                        for (int i = 0; i < coordsX.toArray().length; i++)
                            for (int coordZ : coordsZ) {
                                int claimNum = new Claim().getClaimAt(coordsX.get(i), coordZ, player.getWorld());
                                if (claimNum != -1) {
                                    player.sendMessage(Plugin.prefix + "§cTwo claims can not overlap!");
                                    showOtherClaimParticles(player, claimNum, false);
                                    return true;
                                }
                            }

                        if (pos1.get(uuid)[0] < pos2.get(uuid)[0]) coordsX = new ArrayList<>(Arrays.asList(pos1.get(uuid)[0], pos2.get(uuid)[0]));
                        else coordsX = new ArrayList<>(Arrays.asList(pos2.get(uuid)[0], pos1.get(uuid)[0]));

                        if (pos1.get(uuid)[1] < pos2.get(uuid)[1]) coordsZ = new ArrayList<>(Arrays.asList(pos1.get(uuid)[1], pos2.get(uuid)[1]));
                        else coordsZ = new ArrayList<>(Arrays.asList(pos2.get(uuid)[1], pos1.get(uuid)[1]));

                        int landAmount = 0;
                        int landLeft = new PlayerData(player.getUniqueId()).getLandLeft();
                        for (int x = coordsX.get(0); x <= coordsX.get(1); x++) for (int z = coordsZ.get(0); z <= coordsZ.get(1); z++) landAmount++;

                        if (landLeft < landAmount) {
                            player.sendMessage(Plugin.prefix + "§cYou don't have enough land left to claim!");
                            player.sendMessage(Plugin.prefix + "§cRequired: §e" + landAmount);
                            player.sendMessage(Plugin.prefix + "§cRemaining: §e" + landLeft);
                            return true;
                        }

                        new Claim().addClaim(player, pos1.get(uuid), pos2.get(uuid), player.getWorld(), landAmount);
                        new PlayerData(player.getUniqueId()).setLandLeft(new PlayerData(player.getUniqueId()).getLandLeft() - landAmount);
                        pos1.remove(uuid);
                        pos2.remove(uuid);
                        claimParticles.put(uuid, true);
                        player.sendMessage(Plugin.prefix + "§aLand Claimed!");
                    }
                } else if (args[0].equalsIgnoreCase("cancel")) {
                    pos1.remove(uuid);
                    pos2.remove(uuid);
                    stopParticles(player);
                    player.sendMessage(Plugin.prefix + "§aLand claiming positions removed!");
                } else if (args[0].equalsIgnoreCase("unclaim")) {
                    int claimNum = new Claim().getClaimAt(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), player.getWorld());
                    if (claimNum != -1 && new Claim(claimNum).hasPermissions(player)) {
                        if (unclaimTimer.containsKey(player.getUniqueId())) {
                            new PlayerData(player.getUniqueId()).addLand(new Claim(claimNum).getSize(), false);
                            new Claim(claimNum).removeClaim();
                            player.sendMessage(Plugin.prefix + "§aClaim unclaimed!");
                            stopParticles(player);
                        } else {
                            showOtherClaimParticles(player, claimNum, false);
                            startParticles(player);
                            startUnclaimTimer(player, claimNum);
                            player.sendMessage(Plugin.prefix + "§cAre you sure you want to unclaim this land? type \"/claim unclaim\" again in the next 10 seconds to confirm.");
                        }
                    } else player.sendMessage(Plugin.prefix + "§cYou must be standing in one of your claims to unclaim it!");
                } else return invalidSyntax(player);
            } else if (player.isOp()) {
                 if (args[0].equalsIgnoreCase("getclaim")) {
                    if (args.length == 2) {
                        try {
                            int claimNum = Integer.parseInt(args[1]);
                            Claim claim = new Claim(claimNum);
                            if (claim.getClaimNum() != -1) {
                                String coOwner = "§cN/A";
                                if (!claim.getCoOwner().equalsIgnoreCase("unset")) coOwner = claim.getCoOwnerFormatted();
                                return sendClaimInfo(player, coOwner, claim.getClaimNum(), claim);
                            } else player.sendMessage(Plugin.prefix + "§cEnter a valid claim!");
                        } catch (Exception e) {
                            player.sendMessage(Plugin.prefix + "§cEnter a valid claim!");
                        }
                    } else player.sendMessage(Plugin.prefix + "§cUsage: /claim getclaim <id>");
                } else if (args[0].equalsIgnoreCase("getclaimat")) {
                    if (args.length == 3) {
                        try {
                            int claimNum = new Claim().getClaimAt(Integer.parseInt(args[1]), Integer.parseInt(args[2]), player.getWorld());
                            Claim claim = new Claim(claimNum);
                            if (claimNum != -1) {
                                String coOwner = "§cN/A";
                                if (!claim.getCoOwner().equalsIgnoreCase("unset")) coOwner = claim.getCoOwnerFormatted();
                                showOtherClaimParticles(player, claimNum, true);
                                return sendClaimInfo(player, coOwner, claimNum, claim);
                            } else player.sendMessage(Plugin.prefix + "§cBlock is not part of a claim.");
                            return true;
                        } catch (Exception e) { return claimAtSyntax(player); }
                    } else return claimAtSyntax(player);
                } else return invalidSyntax(player);
            } else return invalidSyntax(player);
        } return true;
    }

    boolean sendClaimInfo(Player player, String coOwner, int claimNum, Claim claim) {
        player.sendMessage("§b---------- §dClaim " + claimNum + " §b------------");
        player.sendMessage("§3- §eOwner: §a" + claim.getOwnerFormatted());
        player.sendMessage("§3- §eCo Owner: §a" + coOwner);
        player.sendMessage("§3- §eCorner 1: §a" + claim.getPos1().toString().replaceAll("\\[", "").replaceAll("]", ""));
        player.sendMessage("§3- §eCorner 2: §a" + claim.getPos2().toString().replaceAll("\\[", "").replaceAll("]", ""));
        player.sendMessage("§3- §eWorld: §a" + claim.getWorld());
        player.sendMessage("§3- §eIs Ancient: §a" + claim.getIsAncient());
        player.sendMessage("§3- §eSize: §a" + claim.getSize());
        player.sendMessage("§b---------- §dClaim " + claimNum + " §b------------");
        return true;
    }

    boolean claimAtSyntax(Player player) {
        player.sendMessage(Plugin.prefix + "§cUsage: /claim getclaimat <x> <z>");
        return true;
    }

    void startUnclaimTimer(Player player, int claimNum) {
        unclaimTimer.put(player.getUniqueId(), claimNum);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            unclaimTimer.remove(player.getUniqueId());
            stopParticles(player);
        }, 200);
    }

    public void showOtherClaimParticles(Player player, Integer claimNum, boolean showParticles) {

        UUID uuid = player.getUniqueId();
        HashMap<UUID, ArrayList<Integer[]>> claimCoords = new HashMap<>();
        claimCoords.put(uuid, new ArrayList<>());
        overlappingClaimCoords.remove(uuid);
        Claim claim = new Claim(claimNum);

        List<Integer> point1 = claim.getPos1();
        List<Integer> point2 = claim.getPos2();

        ArrayList<Integer> coordsX = new ArrayList<>(getCoords(point1.get(0), point2.get(0)));
        ArrayList<Integer> coordsZ = new ArrayList<>(getCoords(point1.get(1), point2.get(1)));

        for (int coordZ : coordsZ) claimCoords.get(uuid).add(new Integer[]{coordsX.get(0), coordZ});
        for (int coordZ : coordsZ) claimCoords.get(uuid).add(new Integer[]{coordsX.get(coordsX.toArray().length-1), coordZ});

        for (int coordX : coordsX) claimCoords.get(uuid).add(new Integer[]{coordX, coordsZ.get(0)});
        for (int coordX : coordsX) claimCoords.get(uuid).add(new Integer[]{coordX, coordsZ.get(coordsZ.toArray().length-1)});

        if (showParticles) for (int i = 0; i < claimCoords.get(uuid).toArray().length - 1; i++) for (int i2 = -2; i2 < 3; i2++)
                player.spawnParticle(Particle.REDSTONE, claimCoords.get(uuid).get(i)[0] + 0.5, player.getLocation().getBlockY() + i2,
                        claimCoords.get(uuid).get(i)[1] + 0.5, 5, new Particle.DustOptions(Color.fromRGB(204, 204, 0), 1.0f));
        else overlappingClaimCoords.put(uuid, claimCoords.get(uuid));
    }

    void ifReadyStartParticles(Player player) {
        boolean playerHasPositionsSelected = (pos1.containsKey(player.getUniqueId()) && pos2.containsKey(player.getUniqueId()));
        UUID uuid = player.getUniqueId();
        if (playerHasPositionsSelected) {
            stopParticles(player);
            outerClaimCoords.put(uuid, new ArrayList<>());

            ArrayList<Integer> coordsX = new ArrayList<>(getCoords(pos1.get(uuid)[0], pos2.get(uuid)[0]));
            ArrayList<Integer> coordsZ = new ArrayList<>(getCoords(pos1.get(uuid)[1], pos2.get(uuid)[1]));

            for (int coordZ : coordsZ) outerClaimCoords.get(uuid).add(new Integer[]{coordsX.get(0), coordZ});
            for (int coordZ : coordsZ) outerClaimCoords.get(uuid).add(new Integer[]{coordsX.get(coordsX.toArray().length-1), coordZ});

            for (int coordX : coordsX) outerClaimCoords.get(uuid).add(new Integer[]{coordX, coordsZ.get(0)});
            for (int coordX : coordsX) outerClaimCoords.get(uuid).add(new Integer[]{coordX, coordsZ.get(coordsZ.toArray().length-1)});

            startParticles(player);

        }
    }

    void startParticles(Player player) {
        UUID uuid = player.getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                runningTasks.get(uuid).add(getTaskId());
                if (outerClaimCoords.containsKey(uuid)) {
                    Particle.DustOptions dustColor = new Particle.DustOptions(Color.fromRGB(204, 0, 0), 1.0f);
                    if (claimParticles.get(uuid)) dustColor = new Particle.DustOptions(Color.fromRGB(0, 204, 0), 1.0f);
                    runningTasks.get(uuid).add(getTaskId());
                    for (int i = 0; i < outerClaimCoords.get(uuid).toArray().length - 1; i++)
                        for (int i2 = -2; i2 < 3; i2++) {
                            player.spawnParticle(Particle.REDSTONE, outerClaimCoords.get(uuid).get(i)[0] + 0.5, player.getLocation().getBlockY() + i2, outerClaimCoords.get(uuid).get(i)[1] + 0.5, 5,
                                    dustColor);
                        }
                } if (overlappingClaimCoords.containsKey(uuid)) {
                    for (int i = 0; i < overlappingClaimCoords.get(uuid).toArray().length - 1; i++) for (int i2 = -2; i2 < 3; i2++)
                        player.spawnParticle(Particle.REDSTONE, overlappingClaimCoords.get(uuid).get(i)[0] + 0.5, player.getLocation().getBlockY() + i2,
                                overlappingClaimCoords.get(uuid).get(i)[1] + 0.5, 5, new Particle.DustOptions(Color.fromRGB(204, 204, 0), 1.0f));
                } if (claimParticles.get(uuid)) {
                    claimParticles.put(uuid, false);
                    stopParticles(player);
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    ArrayList<Integer> getCoords(int pos1, int pos2) {
        ArrayList<Integer> coords = new ArrayList<>();
        int difference = 0;
        int coordToAdd = pos1;
        if (pos1 > pos2) {
            difference = pos1 - pos2;
            coordToAdd = pos2;
        } else if (pos2 > pos1) difference = pos2 - pos1;
        for (int i = 0; i < difference + 1; i++) coords.add(coordToAdd + i);
        return coords;
    }

    void stopParticles(Player player) {
        if (runningTasks.get(player.getUniqueId()).toArray().length > 0) {
            for (int task : runningTasks.get(player.getUniqueId())) Bukkit.getScheduler().cancelTask(task);
            runningTasks.get(player.getUniqueId()).clear();
            outerClaimCoords.remove(player.getUniqueId());
            overlappingClaimCoords.remove(player.getUniqueId());
        }
    }

    boolean invalidSyntax(Player player) {
        player.sendMessage(Plugin.prefix + "§cUsage: /claim <pos1/pos2/confirm/cancel/unclaim>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            ArrayList<String> list = new ArrayList<>(Arrays.asList("pos1", "pos2", "confirm", "cancel", "unclaim"));
            if (sender.isOp()) {
                list.add("getclaimat");
                list.add("getclaim");
            } return list;
        } else {
            if (sender.isOp()) {
                Player player = (Player) sender;
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("getclaimat")) {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(player.getTargetBlock(null, 200).getLocation().getBlockX() + " "
                                + player.getTargetBlock(null, 200).getLocation().getBlockZ());
                        return list;
                    }
                } else if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("getclaimat")) {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(String.valueOf(player.getTargetBlock(null, 200).getLocation().getBlockZ()));
                        return list;
                    }
                }
            }
        } return new ArrayList<>();
    }
}
