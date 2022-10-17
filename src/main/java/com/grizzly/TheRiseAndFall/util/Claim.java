package com.grizzly.TheRiseAndFall.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class Claim {
    List<Integer> pos1;
    List<Integer> pos2;
    boolean isAncient;
    String coOwner;
    String owner;
    String world;
    int claimNum;
    int size;

    public Claim(Integer claimNum) {
        if (claimNum != null && Configs.configs.getClaims().isSet("claim" + claimNum)) {
            this.claimNum = claimNum;
            owner = Configs.configs.getClaims().getString("claim" + claimNum + ".Owner");
            coOwner = Configs.configs.getClaims().getString("claim" + claimNum + ".CoOwner");
            pos1 = Configs.configs.getClaims().getIntegerList("claim" + claimNum + ".pos1");
            pos2 = Configs.configs.getClaims().getIntegerList("claim" + claimNum + ".pos2");
            isAncient = Configs.configs.getClaims().getBoolean("claim" + claimNum + ".Ancient");
            world = Configs.configs.getClaims().getString("claim" + claimNum + ".World");
            size = Configs.configs.getClaims().getInt("claim" + claimNum + ".Size");
        } else {
            this.claimNum = -1;
            owner = null;
            coOwner = null;
            pos1 = null;
            pos2 = null;
            isAncient = false;
            world = null;
            size = -1;
        }
    }

    public Claim() {
        claimNum = -1;
        owner = null;
        coOwner = null;
        pos1 = null;
        pos2 = null;
        world = null;
        isAncient = false;
        size = -1;
    }

    public Claim(Location location) {
        new Claim(location.getBlockX(), location.getBlockZ(), location.getWorld());
    }

    public Claim(Integer x, Integer z, World world) {
        claimNum = getClaimAt(x, z, world);
        if (claimNum != -1) {
            Claim claim = db.claims.get(claimNum);
            owner = claim.owner;
            coOwner = claim.coOwner;
            pos1 = claim.pos1;
            pos2 = claim.pos2;
            isAncient = claim.isAncient;
            this.world = claim.world;
            size = claim.size;
        } else {
            owner = null;
            coOwner = null;
            pos1 = null;
            pos2 = null;
            isAncient = false;
            this.world = null;
            size = -1;
        }
    }

    void cache() {
        db.claims.put(claimNum, this);
    }

    public void save(boolean cache) {
        Configs.configs.getClaims().set("claim" + claimNum + ".Owner", owner);
        Configs.configs.getClaims().set("claim" + claimNum + ".CoOwner", coOwner);
        Configs.configs.getClaims().set("claim" + claimNum + ".pos1", pos1);
        Configs.configs.getClaims().set("claim" + claimNum + ".pos2", pos2);
        Configs.configs.getClaims().set("claim" + claimNum + ".Ancient", isAncient);
        Configs.configs.getClaims().set("claim" + claimNum + ".Size", size);
        Configs.configs.getClaims().set("claim" + claimNum + ".World", world);
        Configs.configs.saveClaims();
        if (cache) cache();
    }

    public Integer getClaimNum() { return claimNum; }
    public String getOwner() { return owner; }
    public String getOwnerFormatted() { return new PlayerData(owner).getName(); }
    public String getCoOwnerFormatted() { return new PlayerData(coOwner).getName(); }
    public String getCoOwner() { return coOwner; }
    public Integer getSize() { return size; }
    public List<Integer> getPos1() { return pos1; }
    public List<Integer> getPos2() { return pos2; }
    public String getWorld() { return world; }
    public Boolean getIsAncient() { return isAncient; }
    public Boolean isValidClaim() { return !(owner == null); }
    public Boolean hasPermissions(Player player) { return (player.getUniqueId().toString().equals(owner) || player.getUniqueId().toString().equals(coOwner) || new PlayerData(player.getUniqueId()).isIgnoringClaims()); }

    public void changeToTeam(Player owner, Player coOwner) {
        this.owner = owner.getUniqueId().toString();
        this.coOwner = coOwner.getUniqueId().toString();
        save(true);
    }

    public int getClaimAt(int blockX, int blockZ, World world) {
        for (Claim claim : db.claims.values()) {
            List<Integer> pos1 = claim.getPos1();
            List<Integer> pos2 = claim.getPos2();

            boolean betweenX = false;
            boolean betweenZ = false;

            if (pos1.get(0) > pos2.get(0)) {
                if (pos1.get(0) >= blockX && blockX >= pos2.get(0)) betweenX = true;
            } else if (pos2.get(0) >= blockX && blockX >= pos1.get(0)) betweenX = true;

            if (pos1.get(1) > pos2.get(1)) {
                if (pos1.get(1) >= blockZ && blockZ >= pos2.get(1)) betweenZ = true;
            } else if (pos2.get(1) >= blockZ && blockZ >= pos1.get(1)) betweenZ = true;

            boolean sameWorld = (claim.getWorld().equals(world.getName()));

            if (betweenX && betweenZ && sameWorld) return claim.claimNum;
        } return -1;
    }

    public void addClaim(Player player, Integer[] pos1, Integer[] pos2, World world, int landAmount) {
        int num = 0;
        while (db.claims.containsKey(num)) num++;
        claimNum = num;
        if (!new Team().isInTeam(player)) {
            owner = player.getUniqueId().toString();
            coOwner = "UNSET";
        } else {
            owner = new Team(player).getOwner();
            coOwner = new Team(player).getCoOwner();
        } this.pos1 = List.of(pos1);
        this.pos2 = List.of(pos2);
        this.world = world.getName();
        isAncient = false;
        size = landAmount;
        save(true);
    }

    public void removeClaim() {
        if (Configs.configs.getClaims().isSet("claim" + claimNum)) {
            Configs.configs.getClaims().set("claim" + claimNum, null);
            Configs.configs.saveClaims();
            db.claims.remove(claimNum);
        }
    }

    public int amountOwned(Player player) {
        String uuidToCheck = player.getUniqueId().toString();
        if (new Team().isInTeam(player)) uuidToCheck = new Team(player).getOwner();
        int amount = 0;
        for (Claim claim : db.claims.values()) if (claim.getOwner().equals(uuidToCheck)) amount++;
        return amount;
    }
}
