package com.grizzly.TheRiseAndFall.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class Team {
    FileConfiguration teams = Configs.configs.getTeams();
    private final String owner;
    private final String coOwner;
    private final Integer livesLeft;
    private final Integer landLeft;

    public Team(Integer teamNum) {
        if (teamNum != null && teams.isSet("team" + teamNum) && teamNum != -1) {
            owner = teams.getString("team" + teamNum + ".Owner");
            coOwner = teams.getString("team" + teamNum + ".CoOwner");
            livesLeft = teams.getInt("team" + teamNum + ".Lives-Left");
            landLeft = teams.getInt("team" + teamNum + ".Land-Left");
        } else {
            owner = null;
            coOwner = null;
            livesLeft = null;
            landLeft = null;
        }
    } public Team() {
        owner = null;
        coOwner = null;
        livesLeft = null;
        landLeft = null;
    } public Team(Player player) {
        int teamNum = new Team().getTeamNum(player);
        if (teams.isSet("team" + teamNum) && teamNum != -1) {
            owner = teams.getString("team" + teamNum + ".Owner");
            coOwner = teams.getString("team" + teamNum + ".CoOwner");
            livesLeft = teams.getInt("team" + teamNum + ".Lives-Left");
            landLeft = teams.getInt("team" + teamNum + ".Land-Left");
        } else {
            owner = null;
            coOwner = null;
            livesLeft = null;
            landLeft = null;
        }
    }

    public String getOwner() { return owner; }
    public String getCoOwner() { return coOwner; }
    public String getOwnerFormatted() { return new PlayerData(owner).getName(); }
    public String getCoOwnerFormatted() { return new PlayerData(coOwner).getName(); }
    public Integer getLivesLeft() { return livesLeft; }
    public Integer getLandLeft() { return landLeft; }
    public Boolean isValidTeam() { return !(owner == null); }
    public void setLandLeft(Player player, Integer amount) {
        teams.set("team" + getTeamNum(player) + ".Land-Left", amount);
        Configs.configs.saveTeams();
    } public void setLivesLeft(Player player, Integer amount) {
        teams.set("team" + getTeamNum(player) + ".Lives-Left", amount);
        Configs.configs.saveTeams();
    }

    public String getName(String uuid) {
        if (isInTeam(uuid)) return teams.getString("team" + getTeamNum(uuid) + ".Name").replaceAll("(&([a-f0-9klmnor]))", "§$2");
        return "§a" + new PlayerData(uuid).getName();
    }

    public void setName(String text) {
        teams.set("team" + getTeamNum(owner) + ".Name", text);
        Configs.configs.saveTeams();
    }

    public int getTeamNum(Player player) {
        String uuid = player.getUniqueId().toString();
        for (String path : teams.getKeys(false)) {
            Team checkTeam = new Team(Integer.parseInt(path.replaceAll("team", "")));
            if (checkTeam.getOwner().equals(uuid) || checkTeam.getCoOwner().equals(uuid)) return Integer.parseInt(path.replaceAll("team", ""));
        } return -1;
    }

    public int getTeamNum(String uuid) {
        for (String path : teams.getKeys(false)) {
            Team checkTeam = new Team(Integer.parseInt(path.replaceAll("team", "")));
            if (checkTeam.getOwner().equals(uuid) || checkTeam.getCoOwner().equals(uuid)) return Integer.parseInt(path.replaceAll("team", ""));
        } return -1;
    }

    public Boolean isInTeam(Player player) { return getTeamNum(player) != -1; }
    public Boolean isInTeam(String uuid) { return getTeamNum(uuid) != -1; }

    public void createTeam(Player owner, Player coOwner) {
        Configs config = Configs.configs;
        int teamNum = 0;
        while (new Team(teamNum).isValidTeam()) teamNum++;
        PlayerData ownerData = new PlayerData(owner.getUniqueId());
        PlayerData coOwnerData = new PlayerData(coOwner.getUniqueId());
        teams.set("team" + teamNum + ".Owner", owner.getUniqueId().toString());
        teams.set("team" + teamNum + ".CoOwner", coOwner.getUniqueId().toString());
        teams.set("team" + teamNum + ".Name", "&a" + owner.getName() + "'s Team");
        teams.set("team" + teamNum + ".Land-Left", ownerData.getInt("Land-Left") + coOwnerData.getInt("Land-Left"));
        teams.set("team" + teamNum + ".Lives-Left", ownerData.getInt("Lives-Left") + coOwnerData.getInt("Lives-Left"));
        String ownerUUID = owner.getUniqueId().toString();
        String coOwnerUUID = coOwner.getUniqueId().toString();
        ArrayList<Claim> claims = new ArrayList<>(db.claims.values());
        for (Claim claim : claims) {
            if (claim.getOwner().equals(ownerUUID) || claim.getCoOwner().equals(ownerUUID) ||
                    claim.getOwner().equals(coOwnerUUID) || claim.getCoOwner().equals(coOwnerUUID))
                claim.changeToTeam(owner, coOwner);
        } for (Shop shop : db.shops.values()) {
            if (shop.owner.equals(ownerUUID) || shop.coOwner.equals(ownerUUID) ||
                    shop.owner.equals(coOwnerUUID) || shop.coOwner.equals(coOwnerUUID)) {
                shop.changeToTeam(owner, coOwner);
                shop.save();
            }
        }/* altar code
        boolean teamAltarExists = false;
        for (String path : config.getAltars().getKeys(false)) {
            if (config.getAltars().get(path + ".Owner").equals(ownerUUID)) {
                config.getAltars().set(path + ".Owner", owner.getUniqueId().toString());
                config.getAltars().set(path + ".CoOwner", coOwner.getUniqueId().toString());
                config.saveAltars();
                teamAltarExists = true;
            } num++;
        } num = 0;
        for (String path : config.getAltars().getKeys(false)) {
            if (config.getAltars().get(path + ".Owner").equals(coOwnerUUID)) {
                if (!teamAltarExists) {
                    config.getAltars().set(path + ".Owner", owner.getUniqueId().toString());
                    config.getAltars().set(path + ".CoOwner", coOwner.getUniqueId().toString());
                    config.saveAltars();
                    teamAltarExists = true;
                } new Altar(coOwner, num).tearDownAltar(true);
                Bukkit.getPlayer(getCoOwnerFormatted()).sendMessage(Plugin.prefix +
                        "§aBecause teammate already had an altar, yours has been taken down! Materials have been returned to the chest.");
            } num++;
        }*/ for (Player player : new Player[]{coOwner, owner}) {
            PlayerData getUserData = new PlayerData(player.getUniqueId());
            getUserData.save();
        } config.saveTeams();
    }

    public void removeTeam() {
        if (owner != null && coOwner != null) {
            ArrayList<Claim> claims = new ArrayList<>(db.claims.values());
            for (Claim claim : claims) {
                if (claim.getOwner().equals(owner) || claim.getCoOwner().equals(owner) ||
                        claim.getOwner().equals(coOwner) || claim.getCoOwner().equals(coOwner)) claim.removeClaim();
            }
            for (Shop shop : db.shops.values()) if (shop.owner.equals(owner) || shop.coOwner.equals(owner) ||
                        shop.owner.equals(coOwner) || shop.coOwner.equals(coOwner)) shop.remove(true);
            teams.set("team" + getTeamNum(owner), null);
            Configs.configs.saveTeams();
        }
    }
}
