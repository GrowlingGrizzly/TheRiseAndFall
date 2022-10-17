package com.grizzly.TheRiseAndFall.util;

import com.grizzly.TheRiseAndFall.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class Altar {

    Main plugin = Main.plugin;

    public ArrayList<Integer[]> outerClaimCoords = new ArrayList<>();
    public final Player sender;
    public final Inventory inventory;
    public Player owner;
    public Player coOwner;
    public Integer altarNum;
    public Material glassType = null;
    Material[] materials = new Material[]{Material.QUARTZ_SLAB, Material.QUARTZ_STAIRS, Material.QUARTZ_BLOCK, Material.GOLD_BLOCK, Material.QUARTZ_PILLAR,
            Material.DIAMOND_BLOCK, Material.SEA_LANTERN, Material.DEEPSLATE_TILE_SLAB, Material.DEEPSLATE_TILE_STAIRS, Material.EMERALD_BLOCK, Material.COBBLED_DEEPSLATE_SLAB,
            Material.NETHERITE_BLOCK, Material.DEEPSLATE_TILE_WALL, Material.CHISELED_DEEPSLATE, Material.CHISELED_QUARTZ_BLOCK, Material.BEACON};
    Integer[] amount = new Integer[]{206, 134, 115, 81, 64, 49, 48, 44, 28, 25, 20, 9, 8, 8, 8, 1};

    public Altar(Player player, Inventory inventory) {
        sender = player;
        this.inventory = inventory;
        save();
    } public Altar(Player player) {
        if (db.currentAltars.containsKey(player.getUniqueId())) {
            sender = db.currentAltars.get(player.getUniqueId()).sender;
            if (db.currentAltars.get(player.getUniqueId()).inventory.getLocation().getBlock().getState() instanceof Chest)
            inventory = ((Chest) db.currentAltars.get(player.getUniqueId()).inventory.getLocation().getBlock().getState()).getBlockInventory();
            else inventory = null;
            glassType = db.currentAltars.get(player.getUniqueId()).glassType;
        } else {
            sender = player;
            inventory = null;
        }
    } public Altar() {
        sender = null;
        inventory = null;
    } public Altar(Player player, Integer altarNum) {
        Configs config = Configs.configs;
        sender = player;
        inventory = null;
        owner = Bukkit.getPlayer(UUID.fromString(config.getAltars().getString("altar" + altarNum + ".Owner")));
        try { coOwner = Bukkit.getPlayer(UUID.fromString(config.getAltars().getString("altar" + altarNum + ".CoOwner"))); } catch (Exception e) { coOwner = null; }
        this.altarNum = altarNum;
        glassType = Material.valueOf(config.getAltars().getString("altar" + altarNum + ".Glass-Type"));
    }

    public void save() { db.currentAltars.put(sender.getUniqueId(), this); }

    public void getReadyToCreate(Boolean finalCheck) {
        if (!finalCheck) {
            stopParticles();
            db.runningAltarBoundaries.remove(sender.getUniqueId());
            db.readyForAltar.remove(sender.getUniqueId());
        } if (db.currentAltars.containsKey(sender.getUniqueId()) && inventory != null && hasNoAltar()) {
            for (int i = 0; i < materials.length; i++) if (!inventory.contains(materials[i], amount[i])) {
                deleteAltarCreation();
                return;
            } Material[] glassTypes = new Material[]{Material.GLASS, Material.RED_STAINED_GLASS, Material.ORANGE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.GREEN_STAINED_GLASS, Material.LIME_STAINED_GLASS,
                    Material.BLUE_STAINED_GLASS, Material.CYAN_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS,
                    Material.PINK_STAINED_GLASS, Material.WHITE_STAINED_GLASS, Material.BLACK_STAINED_GLASS, Material.GRAY_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS, Material.BROWN_STAINED_GLASS};
            for (Material glassType : glassTypes) if (inventory.contains(glassType, 1)) {
                this.glassType = glassType;
                break;
            } if (glassType == null) {
                deleteAltarCreation();
                return;
            } db.readyForAltar.add(sender.getUniqueId());
            if (!finalCheck) readyToConfirm();
        } else db.readyForAltar.remove(sender.getUniqueId());
    }

    void readyToConfirm() {
        sender.sendMessage(Plugin.prefix + "§aItems detected to make an altar! Steer clear of the particle zone and type \"/altar confirm\" or \"/altar cancel.\"\n §c§lWARNING: All blocks in particle zone will be deleted on confirmation!");

        outerClaimCoords.clear();

        ArrayList<Integer> coordsX = new ArrayList<>(getCoords(inventory.getLocation().getBlockX() - 7, inventory.getLocation().getBlockX() + 7));
        ArrayList<Integer> coordsZ = new ArrayList<>(getCoords(inventory.getLocation().getBlockZ() - 7, inventory.getLocation().getBlockZ() + 7));

        for (int coordZ : coordsZ) outerClaimCoords.add(new Integer[]{coordsX.get(0), coordZ});
        for (int coordZ : coordsZ) outerClaimCoords.add(new Integer[]{coordsX.get(coordsX.toArray().length-1), coordZ});

        for (int coordX : coordsX) outerClaimCoords.add(new Integer[]{coordX, coordsZ.get(0)});
        for (int coordX : coordsX) outerClaimCoords.add(new Integer[]{coordX, coordsZ.get(coordsZ.toArray().length-1)});

        new BukkitRunnable() {
            @Override
            public void run() {
                Particle.DustOptions dustColor = new Particle.DustOptions(Color.fromRGB(204, 204, 0), 1.0f);
                db.runningAltarBoundaries.put(sender.getUniqueId(), getTaskId());
                for (int i = 0; i < outerClaimCoords.toArray().length - 1; i++) for (int i2 = 0; i2 < 14; i2++)
                    sender.spawnParticle(Particle.REDSTONE, outerClaimCoords.get(i)[0] + 0.5, inventory.getLocation().getBlockY() + i2 + 0.5,
                            outerClaimCoords.get(i)[1] + 0.5, 5, dustColor);
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    public void createAltar() {
        Configs config = Configs.configs;
        List<Integer> center = Arrays.asList(inventory.getLocation().getBlockX(), inventory.getLocation().getBlockY(), inventory.getLocation().getBlockZ());
        for (int x = center.get(0) - 7; x <= center.get(0) + 7; x++)
            for (int y = center.get(1); y <= center.get(1) + 13; y++)
                for (int z = center.get(2) - 7; z <= center.get(2) + 7; z++) new Location(sender.getWorld(), x, y, z).getBlock().setType(Material.AIR);
        for (Integer[] loc : new Integer[][]{new Integer[]{4, 0, 6}, new Integer[]{3, 0, 6}, new Integer[]{2, 0, 6}, new Integer[]{6, 0, 4}, new Integer[]{6, 0, 3}, new Integer[]{6, 0, 2},
                new Integer[]{5, 2, 5}, new Integer[]{4, 3, 4}, new Integer[]{6, 8, 4}, new Integer[]{6, 8, 5}, new Integer[]{6, 8, 6}, new Integer[]{5, 8, 6}, new Integer[]{4, 8, 6},
                new Integer[]{1, 8, 7}, new Integer[]{0, 8, 7}, new Integer[]{7, 8, 0}, new Integer[]{7, 8, 1}, new Integer[]{1, 9, 6}, new Integer[]{0, 9, 6}, new Integer[]{6, 9, 0},
                new Integer[]{6, 9, 1}, new Integer[]{2, 9, 4}, new Integer[]{2, 9, 5}, new Integer[]{3, 9, 4}, new Integer[]{3, 9, 5}, new Integer[]{5, 9, 3}, new Integer[]{4, 9, 3},
                new Integer[]{5, 9, 2}, new Integer[]{4, 9, 2}, new Integer[]{0, 10, 4}, new Integer[]{4, 10, 0}, new Integer[]{3, 11, 3}, new Integer[]{2, 12, 2}, new Integer[]{1, 13, 1},
                new Integer[]{1, 12, 3}, new Integer[]{3, 12, 1}}) createSlab(loc[0], loc[1], loc[2], Material.QUARTZ_SLAB, true);
        for (Integer[] loc : new Integer[][]{new Integer[]{7, 7, 7}, new Integer[]{2, 7, 7}, new Integer[]{2, 7, 6}, new Integer[]{1, 7, 6}, new Integer[]{0, 7, 6}, new Integer[]{6, 7, 0},
                new Integer[]{6, 7, 1}, new Integer[]{6, 7, 2}, new Integer[]{7, 7, 2}, new Integer[]{5, 8, 4}, new Integer[]{5, 8, 5}, new Integer[]{4, 8, 5}, new Integer[]{4, 8, 4},
                new Integer[]{1, 9, 5}, new Integer[]{1, 9, 4}, new Integer[]{4, 9, 1}, new Integer[]{5, 9, 1}, new Integer[]{2, 11, 3}, new Integer[]{3, 11, 2}, new Integer[]{0, 12, 2},
                new Integer[]{2, 12, 0}}) createSlab(loc[0], loc[1], loc[2], Material.QUARTZ_SLAB, false);
        for (Integer[] loc : new Integer[][]{new Integer[]{1, 0, 5}, new Integer[]{2, 0, 5}, new Integer[]{3, 0, 5}, new Integer[]{4, 0, 5}, new Integer[]{5, 0, 5},
                new Integer[]{5, 0, 4}, new Integer[]{5, 0, 3}, new Integer[]{5, 0, 2}, new Integer[]{5, 0, 1}, new Integer[]{1, 8, 6}, new Integer[]{0, 8, 6}, new Integer[]{6, 8, 1},
                new Integer[]{6, 8, 0}}) createBlock(loc[0], loc[1], loc[2], Material.SEA_LANTERN);
        for (Integer[] loc : new Integer[][]{new Integer[]{0, 0, 5}, new Integer[]{5, 0, 0}, new Integer[]{0, 9, 4}, new Integer[]{4, 9, 0}, new Integer[]{5, 7, 6}, new Integer[]{4, 7, 6},
                new Integer[]{3, 7, 6}, new Integer[]{6, 7, 3}, new Integer[]{6, 7, 4}, new Integer[]{6, 7, 5}}) createBlock(loc[0], loc[1], loc[2], Material.QUARTZ_BLOCK);
        for (Integer[] loc : new Integer[][]{new Integer[]{6, 0, 6}, new Integer[]{6, 1, 6}, new Integer[]{6, 2, 6}, new Integer[]{6, 3, 6}, new Integer[]{6, 4, 6},
                new Integer[]{6, 5, 6}, new Integer[]{6, 6, 6}, new Integer[]{6, 7, 6}, new Integer[]{3, 3, 3}, new Integer[]{3, 4, 3}, new Integer[]{3, 5, 3},
                new Integer[]{3, 6, 3}, new Integer[]{3, 7, 3}, new Integer[]{3, 8, 3}, new Integer[]{3, 9, 3}, new Integer[]{3, 10, 3}})
            createBlock(loc[0], loc[1], loc[2], Material.QUARTZ_PILLAR);
        for (Integer[] loc : new Integer[][]{new Integer[]{1, 5, 1}, new Integer[]{2, 4, 2}}) createBlock(loc[0], loc[1], loc[2], Material.CHISELED_DEEPSLATE);
        for (Integer[] loc : new Integer[][]{new Integer[]{1, 6, 1}, new Integer[]{1, 7, 1}}) createBlock(loc[0], loc[1], loc[2], Material.DEEPSLATE_TILE_WALL);
        for (Integer[] loc : new Integer[][]{new Integer[]{5, 1, 5}, new Integer[]{4, 2, 4}}) createBlock(loc[0], loc[1], loc[2], Material.CHISELED_QUARTZ_BLOCK);
        for (Integer[] loc : new Integer[][]{new Integer[]{4, 1, 5}, new Integer[]{3, 1, 5}, new Integer[]{2, 1, 5}, new Integer[]{5, 1, 4}, new Integer[]{5, 1, 3},
                new Integer[]{5, 1, 2}, new Integer[]{2, 2, 4}, new Integer[]{3, 2, 4}, new Integer[]{4, 2, 2}, new Integer[]{4, 2, 3}, new Integer[]{1, 8, 1}})
            createBlock(loc[0], loc[1], loc[2], Material.DEEPSLATE_TILE_SLAB);
        for (Integer[] loc : new Integer[][]{new Integer[]{0, 10, 3}, new Integer[]{0, 10, 2}, new Integer[]{0, 10, 1}, new Integer[]{1, 10, 0},
                new Integer[]{2, 10, 0}, new Integer[]{3, 10, 0}}) createSlab(loc[0], loc[1], loc[2], Material.COBBLED_DEEPSLATE_SLAB, true);
        for (Integer[] loc : new Integer[][]{new Integer[]{1, 10, 2}, new Integer[]{2, 10, 1}}) createSlab(loc[0], loc[1], loc[2], Material.COBBLED_DEEPSLATE_SLAB, false);
        for (Integer[] loc : new Integer[][]{new Integer[]{4, 8, 0}, new Integer[]{0, 8, 4}}) createBlock(loc[0], loc[1], loc[2], Material.SPORE_BLOSSOM);
        for (Integer[] loc : new Integer[][]{new Integer[]{6, 0}, new Integer[]{3, 3}}) createOppositeSideStairs(loc[0], loc[1], Material.QUARTZ_STAIRS);
        for (Integer[] loc : new Integer[][]{new Integer[]{5, 1}, new Integer[]{4, 2}, new Integer[]{2, 4}}) createOppositeSideStairs(loc[0], loc[1], Material.DEEPSLATE_TILE_STAIRS);
        for (Integer[] loc : new Integer[][]{new Integer[]{0, 0, 6}, new Integer[]{0, 1, 5}, new Integer[]{0, 2, 4}, new Integer[]{0, 3, 3}, new Integer[]{0, 4, 2},
                new Integer[]{0, 5, 1}, new Integer[]{0, 12, 3}, new Integer[]{0, 13, 1}}) createStairs(loc[0], loc[1], loc[2], Material.QUARTZ_STAIRS, true, false);
        for (Integer[] loc : new Integer[][]{new Integer[]{0, 9, 5}}) createStairs(loc[0], loc[1], loc[2], Material.QUARTZ_STAIRS, true, true);
        for (Integer[] loc : new Integer[][]{new Integer[]{3, 8, 6}, new Integer[]{2, 8, 6}, new Integer[]{6, 7, 7},
                new Integer[]{5, 7, 7}, new Integer[]{4, 7, 7}, new Integer[]{3, 7, 7}}) createStairs(loc[0], loc[1], loc[2], Material.QUARTZ_STAIRS, false, true);



        fillAreaWithBlock(-4, 4, 0, -4, 4, Material.QUARTZ_BLOCK);
        fillAreaWithBlock(-4, 4, 1, -4, 4, Material.GOLD_BLOCK);
        fillAreaWithBlock(-3, 3, 2, -3, 3, Material.DIAMOND_BLOCK);
        fillAreaWithBlock(-2, 2, 3, -2, 2, Material.EMERALD_BLOCK);
        fillAreaWithBlock(-1, 1, 4, -1, 1, Material.NETHERITE_BLOCK);
        createStairs(1, 12, 2, Material.QUARTZ_STAIRS, false, false);
        createStairs(0, 8, 1, Material.DEEPSLATE_TILE_STAIRS, true, false);
        createBlock(0, 5, 0, Material.BEACON);
        createBlock(0, 13, 0, glassType);

        for (Integer[] location : new Integer[][]{new Integer[]{6, 0, 6}, new Integer[]{6*-1, 0, 6}, new Integer[]{6, 0, 6*-1}, new Integer[]{6*-1, 0, 6*-1}}) {
            int x = getRelativeLocation(location[0], location[1], location[2]).getBlockX();
            int y = getRelativeLocation(location[0], location[1], location[2]).getBlockY();
            int z = getRelativeLocation(location[0], location[1], location[2]).getBlockZ();
            for (Integer[] loc : new Integer[][]{new Integer[]{1, 0, 0, 0}, new Integer[]{-1, 0, 0, 1}, new Integer[]{0, 0, 1, 2},
                    new Integer[]{0, 0, -1, 3}}) {
                Block block = getRelativeLocationFromBlock(x, y, z, loc[0], loc[1], loc[2]).getBlock();
                block.setType(Material.QUARTZ_STAIRS);
                Stairs stairs = (Stairs) block.getBlockData();
                switch (loc[3]) {
                    case 0: stairs.setFacing(BlockFace.WEST); break;
                    case 1: stairs.setFacing(BlockFace.EAST); break;
                    case 2: stairs.setFacing(BlockFace.NORTH); break;
                    case 3: stairs.setFacing(BlockFace.SOUTH); break;
                } block.setBlockData(stairs);
            }
        } for (Integer[] loc : new Integer[][]{new Integer[]{-2, 3, 3, 0}, new Integer[]{-2, 3, -3, 0}, new Integer[]{2, 3, -3, 1}, new Integer[]{2, 3, 3, 1},
                new Integer[]{3, 3, 2, 2}, new Integer[]{-3, 3, 2, 2}, new Integer[]{-3, 3, -2, 3}, new Integer[]{3, 3, -2, 3}}){
            Block block = getRelativeLocation(loc[0], loc[1], loc[2]).getBlock();
            block.setType(Material.QUARTZ_STAIRS);
            Stairs stairs = (Stairs) block.getBlockData();
            switch (loc[3]) {
                case 0: stairs.setFacing(BlockFace.WEST); break;
                case 1: stairs.setFacing(BlockFace.EAST); break;
                case 2: stairs.setFacing(BlockFace.SOUTH); break;
                case 3: stairs.setFacing(BlockFace.NORTH); break;
            } block.setBlockData(stairs);
        }

        String id = "altar" + GetAltarId();
        if (!new Team().isInTeam(sender)) {
            config.getAltars().set(id + ".Owner", sender.getUniqueId().toString());
            config.getAltars().set(id + ".CoOwner", "UNSET");
        } else if (new Team().isInTeam(sender)) {
            config.getAltars().set(id + ".Owner", new Team(sender).getOwner());
            config.getAltars().set(id + ".CoOwner", new Team(sender).getCoOwner());
        } config.getAltars().set(id + ".Center", new Integer[]{inventory.getLocation().getBlockX(), inventory.getLocation().getBlockY(), inventory.getLocation().getBlockZ()});
        config.getAltars().set(id + ".Glass-Type", glassType.toString());
        config.saveAltars();
        deleteAltarCreation();
        sender.sendMessage(Plugin.prefix + "§aAltar created!");
    }

    void createOppositeSideStairs(Integer num, Integer y, Material mat) {
        for (Integer[] location : new Integer[][]{new Integer[]{0, y, num, 0}, new Integer[]{0, y, num*-1, 0}, new Integer[]{num, y, 0, 1}, new Integer[]{num*-1, y, 0, 1}}) {
            int x = getRelativeLocation(location[0], location[1], location[2]).getBlockX();
            y = getRelativeLocation(location[0], location[1], location[2]).getBlockY();
            int z = getRelativeLocation(location[0], location[1], location[2]).getBlockZ();
            for (Integer[] loc : new Integer[][]{new Integer[]{1, 0}, new Integer[]{-1, 1}}) {
                Block block;
                if (location[3].equals(0)) block = getRelativeLocationFromBlock(x, y, z, loc[0], 0, 0).getBlock();
                else block = getRelativeLocationFromBlock(x, y, z, 0, 0, loc[0]).getBlock();
                block.setType(mat);
                Stairs stairs = (Stairs) block.getBlockData();
                if (location[3].equals(0)) switch (loc[1]) {
                    case 0: stairs.setFacing(BlockFace.WEST); break;
                    case 1: stairs.setFacing(BlockFace.EAST); break;
                } else switch (loc[1]) {
                    case 0: stairs.setFacing(BlockFace.NORTH); break;
                    case 1: stairs.setFacing(BlockFace.SOUTH); break;
                } block.setBlockData(stairs);
            }
        }
    }

    void createStairs(Integer x, Integer y, Integer z, Material mat, boolean centered, boolean isTopHalf) {
        if (centered) for (Integer[] loc : new Integer[][]{new Integer[]{0, y, z, 0}, new Integer[]{z, y, 0, 1}, new Integer[]{0, y, z*-1, 2}, new Integer[]{z*-1, y, 0, 3}}) {
            Block block = getRelativeLocation(loc[0], loc[1], loc[2]).getBlock();
            block.setType(mat);
            Stairs stairs = (Stairs) block.getBlockData();
            switch (loc[3]) {
                case 0: stairs.setFacing(BlockFace.NORTH); break;
                case 1: stairs.setFacing(BlockFace.WEST); break;
                case 2: stairs.setFacing(BlockFace.SOUTH); break;
                case 3: stairs.setFacing(BlockFace.EAST); break;
            } if (isTopHalf) stairs.setHalf(Bisected.Half.TOP);
            else stairs.setHalf(Bisected.Half.BOTTOM);
            block.setBlockData(stairs);
        } else for (Integer[] loc : new Integer[][]{new Integer[]{x, y, z, 0}, new Integer[]{x*-1, y, z, 0}, new Integer[]{x, y, z*-1, 1}, new Integer[]{x*-1, y, z*-1, 1},
                new Integer[]{z, y, x, 2}, new Integer[]{z, y, x*-1, 2}, new Integer[]{z*-1, y, x, 3}, new Integer[]{z*-1, y, x*-1, 3}}){
            Block block = getRelativeLocation(loc[0], loc[1], loc[2]).getBlock();
            block.setType(mat);
            Stairs stairs = (Stairs) block.getBlockData();
            switch (loc[3]) {
                case 0: stairs.setFacing(BlockFace.NORTH); break;
                case 1: stairs.setFacing(BlockFace.SOUTH); break;
                case 2: stairs.setFacing(BlockFace.WEST); break;
                case 3: stairs.setFacing(BlockFace.EAST); break;
            } if (isTopHalf) stairs.setHalf(Bisected.Half.TOP);
            else stairs.setHalf(Bisected.Half.BOTTOM);
            block.setBlockData(stairs);
        }
    }


    void createSlab(Integer x, Integer y, Integer z, Material mat, Boolean isBottom) {
        for (Integer[] loc : new Integer[][]{new Integer[]{x, y, z}, new Integer[]{x*-1, y, z}, new Integer[]{x, y, z*-1}, new Integer[]{x*-1, y, z*-1}}) {
            Block block = getRelativeLocation(loc[0], loc[1], loc[2]).getBlock();
            block.setType(mat);
            Slab slab = (Slab) block.getBlockData();
            if (isBottom) slab.setType(Slab.Type.BOTTOM);
            else slab.setType(Slab.Type.TOP);
            block.setBlockData(slab);
        }
    }

    void createBlock(Integer x, Integer y, Integer z, Material mat) {
        for (Integer[] loc : new Integer[][]{new Integer[]{x, y, z}, new Integer[]{x*-1, y, z}, new Integer[]{x, y, z*-1}, new Integer[]{x*-1, y, z*-1}})
            getRelativeLocation(loc[0], loc[1], loc[2]).getBlock().setType(mat);
    }

    void fillAreaWithBlock(Integer x1, Integer x2, Integer y, Integer z1, Integer z2, Material mat) {
        for (int x = x1; x <= x2; x++) for (int z = z1; z <= z2; z++) getRelativeLocation(x, y, z).getBlock().setType(mat);
    }

    public boolean isInClaim() {
        Location pos1 = getRelativeLocation(-7, 0, -7);
        Location pos2 = getRelativeLocation(7, 0, 7);

        for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) if (new Claim().getClaimAt(x, z, pos1.getWorld()) == -1) return false;
        return true;
    }

    public void deleteAltarCreation() {
        stopParticles();
        db.runningAltarBoundaries.remove(sender.getUniqueId());
        db.readyForAltar.remove(sender.getUniqueId());
        db.currentAltars.remove(sender.getUniqueId());
    }


    public void stopParticles() {
        if (db.runningAltarBoundaries.containsKey(sender.getUniqueId()))
            Bukkit.getScheduler().cancelTask(db.runningAltarBoundaries.get(sender.getUniqueId()));
    }

    public Boolean hasPermission() { return (sender == owner || (coOwner != null && sender == coOwner)); }

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


    public boolean hasNoAltar() {
        Configs config = Configs.configs;
        for (String path : config.getAltars().getKeys(false)) {
            if (new Team().isInTeam(sender)) if (config.getAltars().getString(path + ".Owner")
                    .equals(Bukkit.getPlayer(new Team(sender).getOwnerFormatted()).getUniqueId().toString())) return false;
            if (!new Team().isInTeam(sender)) if (config.getAltars().getString(path + ".Owner")
                    .equals(sender.getUniqueId().toString())) return false;
        } return true;
    }

    Location getRelativeLocationFromBlock(int x, int y, int z, int offX, int offY, int offZ) {
        return new Location(inventory.getLocation().getWorld(), x + offX, y + offY, z + offZ);
    }

    Location getRelativeLocation(int offX, int offY, int offZ) {
        return new Location(inventory.getLocation().getWorld(), inventory.getLocation().getBlockX() + offX,
                inventory.getLocation().getBlockY() + offY, inventory.getLocation().getBlockZ() + offZ);
    }

    Integer GetAltarId() {
        int altarNum = 0;
        while (Configs.configs.getAltars().contains("altar" + altarNum)) altarNum++;
        return altarNum;
    }

    public void tearDownAltar(boolean force) {
        Configs config = Configs.configs;
        List<Integer> center = config.getAltars().getIntegerList("altar" + altarNum + ".Center");
        for (int x = center.get(0) - 7; x <= center.get(0) + 7; x++)
            for (int y = center.get(1); y <= center.get(1) + 13; y++)
                for (int z = center.get(2) - 7; z <= center.get(2) + 7; z++) new Location(sender.getWorld(), x, y, z).getBlock().setType(Material.AIR);
        new Location(sender.getWorld(), center.get(0), center.get(1), center.get(2)).getBlock().setType(Material.CHEST);
        Inventory inv = ((Chest) new Location(sender.getWorld(), center.get(0), center.get(1), center.get(2)).getBlock().getState()).getBlockInventory();
        for (int i = 0; i < materials.length; i++) inv.addItem(new ItemMaker().create("", materials[i], null, amount[i]));
        inv.addItem(new ItemMaker().create("", glassType, null));

        config.getAltars().set("altar" + altarNum, null);
        Configs.configs.saveAltars();

        if (!force) sender.sendMessage(Plugin.prefix + "§aAltar has been taken down!");
    }

    public Integer getAltarAt(Integer blockX, Integer blockY, Integer blockZ) {
        Configs config = Configs.configs;
        for (String path : config.getAltars().getKeys(false)) {
            List<Integer> center = config.getAltars().getIntegerList(path + ".Center");
            List<Integer> pos1 = new ArrayList<>(Arrays.asList(center.get(0) + 7, center.get(1), center.get(2) + 7));
            List<Integer> pos2 = new ArrayList<>(Arrays.asList(center.get(0) - 7, center.get(1) + 13, center.get(2) - 7));

            boolean betweenX = false;
            boolean betweenY = false;
            boolean betweenZ = false;

            if (pos1.get(0) > pos2.get(0)) {
                if (pos1.get(0) >= blockX && blockX >= pos2.get(0)) betweenX = true;
            } else if (pos2.get(0) >= blockX && blockX >= pos1.get(0)) betweenX = true;


            if (pos1.get(1) > pos2.get(1)) {
                if (pos1.get(1) >= blockY && blockY >= pos2.get(1)) betweenY = true;
            } else if (pos2.get(1) >= blockY && blockY >= pos1.get(1)) betweenY = true;

            if (pos1.get(2) > pos2.get(2)) {
                if (pos1.get(2) >= blockZ && blockZ >= pos2.get(2)) betweenZ = true;
            } else if (pos2.get(2) >= blockZ && blockZ >= pos1.get(2)) betweenZ = true;


            if (betweenX && betweenY && betweenZ) return Integer.parseInt(path.replaceAll("altar", ""));
        }
        return -1;
    }

}
