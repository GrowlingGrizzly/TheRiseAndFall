package com.grizzly.TheRiseAndFall.util;

import com.grizzly.TheRiseAndFall.Main;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Configs {

    FileConfiguration heads = new YamlConfiguration();
    FileConfiguration songs = new YamlConfiguration();
    FileConfiguration commissions;
    FileConfiguration config;
    FileConfiguration claims;
    FileConfiguration altars;
    FileConfiguration backup;
    FileConfiguration shops;
    FileConfiguration teams;
    File commissionsConfig;
    File pluginConfig;
    File claimsConfig;
    File altarsConfig;
    File backupConfig;
    File shopsConfig;
    File teamsConfig;

    public static Configs configs;
    Main plugin;

    public Configs(Main plugin) { 
        configs = this;
        this.plugin = plugin;
    }

    public FileConfiguration getCommissions() { return commissions; }
    public FileConfiguration getInvBackup() { return backup; }
    public FileConfiguration getAltars() { return altars; }
    public FileConfiguration getConfig() { return config; }
    public FileConfiguration getClaims() { return claims; }
    public FileConfiguration getTeams() { return teams; }
    public FileConfiguration getHeads() { return heads; }
    public FileConfiguration getShops() { return shops; }
    public FileConfiguration getSongs() { return songs; }

    public void startup() {
        createCommissionsFile();
        createDefaultConfig();
        loadInternalFiles();
        createClaimsFile();
        createAltarsFile();
        createBackupFile();
        createShopsFile();
        createTeamsFile();
    }

    public void loadInternalFiles() {
        try {
            heads.load(new InputStreamReader(plugin.getResource("heads.yml"), StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        } try {
            songs.load(new InputStreamReader(plugin.getResource("games/songs.yml"), StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createDefaultConfig() {
        pluginConfig = new File(plugin.getDataFolder(), "config.yml");
        if (!pluginConfig.exists())
            plugin.saveResource("config.yml", false);
        config = new YamlConfiguration();
        try {
            config.load(pluginConfig);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    } public void reloadDefaultConfig() {
        config = YamlConfiguration.loadConfiguration(pluginConfig);
        InputStream defConfigStream = plugin.getResource("config.yml");
        if (defConfigStream != null)
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream)));
    } public void saveConfig() {
        try { config.save(pluginConfig); reloadDefaultConfig(); } catch (IOException e) { e.printStackTrace(); }
    }

    public void createClaimsFile() {
        claimsConfig = new File(plugin.getDataFolder(), "claims.yml");
        if (!claimsConfig.exists())
            plugin.saveResource("claims.yml", false);
        claims = new YamlConfiguration();
        try {
            claims.load(claimsConfig);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    } public void reloadClaims() {
        claims = YamlConfiguration.loadConfiguration(claimsConfig);
        InputStream defConfigStream = plugin.getResource("claims.yml");
        if (defConfigStream != null)
            claims.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream)));
    } public void saveClaims() {
        try { claims.save(claimsConfig); reloadClaims(); } catch (IOException e) { e.printStackTrace(); }
    }

    public void createTeamsFile() {
        teamsConfig = new File(plugin.getDataFolder(), "teams.yml");
        if (!teamsConfig.exists())
            plugin.saveResource("teams.yml", false);
        teams = new YamlConfiguration();
        try {
            teams.load(teamsConfig);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    } public void reloadTeams() {
        teams = YamlConfiguration.loadConfiguration(teamsConfig);
        InputStream defConfigStream = plugin.getResource("teams.yml");
        if (defConfigStream != null)
            teams.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream)));
    } public void saveTeams() {
        try { teams.save(teamsConfig); reloadTeams(); } catch (IOException e) { e.printStackTrace(); }
    }

    public void createCommissionsFile() {
        commissionsConfig = new File(plugin.getDataFolder(), "commissions.yml");
        if (!commissionsConfig.exists())
            plugin.saveResource("commissions.yml", false);
        commissions = new YamlConfiguration();
        try {
            commissions.load(commissionsConfig);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    } public void reloadCommissions() {
        commissions = YamlConfiguration.loadConfiguration(commissionsConfig);
        InputStream defConfigStream = plugin.getResource("commissions.yml");
        if (defConfigStream != null)
            commissions.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream)));
    }

    public void createAltarsFile() {
        altarsConfig = new File(plugin.getDataFolder(), "altars.yml");
        if (!altarsConfig.exists())
            plugin.saveResource("altars.yml", false);
        altars = new YamlConfiguration();
        try {
            altars.load(altarsConfig);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    } public void reloadAltars() {
        altars = YamlConfiguration.loadConfiguration(altarsConfig);
        InputStream defConfigStream = plugin.getResource("altars.yml");
        if (defConfigStream != null)
            altars.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream)));
    } public void saveAltars() {
        try { altars.save(altarsConfig); reloadAltars(); } catch (IOException e) { e.printStackTrace(); }
    }

    public void createBackupFile() {
        backupConfig = new File(plugin.getDataFolder(), "games/inv-backup.yml");
        if (!backupConfig.exists())
            plugin.saveResource("games/inv-backup.yml", false);
        backup = new YamlConfiguration();
        try {
            backup.load(backupConfig);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    } public void reloadInvBackup() {
        backup = YamlConfiguration.loadConfiguration(backupConfig);
        InputStream defConfigStream = plugin.getResource("games/inv-backup.yml");
        if (defConfigStream != null)
            backup.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream)));
    } public void saveInvBackup() {
        try { backup.save(backupConfig); reloadInvBackup(); } catch (IOException e) { e.printStackTrace(); }
    }

    public void createShopsFile() {
        shopsConfig = new File(plugin.getDataFolder(), "shops.yml");
        if (!shopsConfig.exists())
            plugin.saveResource("shops.yml", false);
        shops = new YamlConfiguration();
        try {
            shops.load(shopsConfig);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    } public void reloadShops() {
        shops = YamlConfiguration.loadConfiguration(shopsConfig);
        InputStream defConfigStream = plugin.getResource("shops.yml");
        if (defConfigStream != null)
            shops.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream)));
    } public void saveShops() {
        try { shops.save(shopsConfig); reloadInvBackup(); } catch (IOException e) { e.printStackTrace(); }
    }

}
