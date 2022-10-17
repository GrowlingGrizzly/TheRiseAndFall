package com.grizzly.TheRiseAndFall.util;

import com.grizzly.TheRiseAndFall.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class PlayerData extends YamlConfiguration {
    private final JavaPlugin plugin;
    private final String fileName;
    private final String dir;
    public PlayerData(UUID uuid) {
        this.plugin = Main.plugin;
        this.fileName = uuid + ".yml";
        this.dir = plugin.getDataFolder() + "/userdata/";
        createFile();
    } public PlayerData(String uuid) {
        this.plugin = Main.plugin;
        this.fileName = uuid + ".yml";
        this.dir = plugin.getDataFolder() + "/userdata/";
        createFile();
    } private void createFile() {
        try {
            File file = new File(dir, fileName);
            if (!file.exists()) {
                if (plugin.getResource(fileName) != null) plugin.saveResource(fileName, false);
                else save(file);
            } else {
                load(file);
                save(file);
            }
        } catch (Exception e) {e.printStackTrace(); }
    } public void save() { try { save(new File(dir, fileName)); } catch (Exception e) { e.printStackTrace(); } }
    public void putIfAbsent(String key, Object value) { if (!contains(key)) set(key, value); }
    public int getLandLeft() {
        Player player = Bukkit.getPlayer(getName());
        if (player == null) return -1;
        if (new Team().isInTeam(player)) return new Team(player).getLandLeft();
        else return getInt("Land-Left");
    } public int getLivesLeft() {
        Player player = Bukkit.getPlayer(getName());
        if (player == null) return -1;
        if (new Team().isInTeam(player)) return new Team(player).getLivesLeft();
        else return getInt("Lives-Left");
    } public void setLivesLeft(Integer amount) {
        Player player = Bukkit.getPlayer(getName());
        if (player == null) return;
        if (new Team().isInTeam(player)) new Team().setLivesLeft(player, amount);
        set("Lives-Left", amount);
        save();
    } public void setLandLeft(Integer amount) {
        Player player = Bukkit.getPlayer(getName());
        if (player == null) return;
        if (new Team().isInTeam(player)) new Team().setLandLeft(player, amount);
        else set("Land-Left", amount);
        save();
    } public void addLand(Integer amount, boolean addToBoth) {
        Player player = Bukkit.getPlayer(getName());
        if (player == null) return;
        if (new Team().isInTeam(player)) new Team().setLandLeft(player, amount + new Team(player).getLandLeft());
        if (!new Team().isInTeam(player) || addToBoth) {
            set("Land-Left", amount + getInt("Land-Left"));
            save();
        }
    } public String getName() {
        if (getString("Last-Account-Name") == null) return "§cUnknown/Invalid";
        return getString("Last-Account-Name");
    } public void setIgnoringClaims(boolean ignoring) {
        set("Ignoring-Claims", ignoring);
        save();
    } public boolean isIgnoringClaims() { return getBoolean("Ignoring-Claims"); }
}