package com.grizzly.TheRiseAndFall.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class Commission {

    final Player player;
    final Integer amountCompleted;
    final Integer commNum;
    final Integer listNum;
    final String name;
    final Material item;
    final Integer amount;
    final Integer progress;
    final String rewardType;
    final Material rewardItem;
    final Integer rewardAmount;

    public Commission(Player player, Integer commissionNumber) {
        PlayerData getUserData = new PlayerData(player.getUniqueId());
        String path = "Commissions." + commissionNumber + ".";
        this.player = player;
        amountCompleted = getUserData.getInt("Commissions.Completed");
        commNum = commissionNumber;
        listNum = getUserData.getInt(path + "Number");
        name = getUserData.getString(path + "Name");
        item = Material.valueOf(getUserData.getString(path + "Item"));
        amount = getUserData.getInt(path + "Amount");
        progress = getUserData.getInt(path + "Progress");
        rewardType = getUserData.getString(path + "Reward.Type");
        if (getUserData.contains(path + "Reward.Item")) rewardItem = Material.valueOf(getUserData.getString(path + "Reward.Item"));
        else rewardItem = null;
        rewardAmount = getUserData.getInt(path + "Reward.Amount");
    } public Commission(Player player) {
        this.player = player;
        amountCompleted = new PlayerData(player.getUniqueId()).getInt("Commissions.Completed");
        commNum = -1;
        listNum = -1;
        name = null;
        item = null;
        amount = -1;
        progress = -1;
        rewardType = null;
        rewardItem = null;
        rewardAmount = -1;
    }

    public Integer getAmountCompleted() { return amountCompleted; }
    public String getName() { return name; }
    public Material getItem() { return item; }
    public Integer getAmount() { return amount; }
    public Integer getProgress() { return progress; }
    public String getRewardType() { return rewardType; }
    public Material getRewardItem() { return rewardItem; }
    public Integer getRewardAmount() { return rewardAmount; }

    public void createDefaults(boolean override) {
        PlayerData getUserData = new PlayerData(player.getUniqueId());
        if (!override) {
            if (!getUserData.contains("Commissions.0")) createSingle(0, null);
            if (!getUserData.contains("Commissions.1")) createSingle(1, null);
        } else {
            createSingle(0, null);
            createSingle(1, null);
        }
    }

    public void createSingle(Integer commNum, String id) {
        Configs config = Configs.configs;
        PlayerData getUserData = new PlayerData(player.getUniqueId());
        if (id == null) id = getRandomCommission();
        String path = "Commissions." + commNum + ".";
        String path2 = "Commissions." + id + ".";
        getUserData.set(path + "Id", id);
        getUserData.set(path + "Name", config.getCommissions().get(path2 + "Name"));
        getUserData.set(path + "Item", config.getCommissions().get(path2 + "Item"));
        getUserData.set(path + "Amount", config.getCommissions().get(path2 + "Amount"));
        getUserData.set(path + "Progress", 0);
        getUserData.set(path + "Reward.Type", config.getCommissions().get(path2 + "Reward.Type"));
        if (config.getCommissions().contains(path2 + "Reward.Item"))
            getUserData.set(path + "Reward.Item", config.getCommissions().get(path2 + "Reward.Item"));
        else getUserData.set(path + "Reward.Item", null);
        getUserData.set(path + "Reward.Amount", config.getCommissions().get(path2 + "Reward.Amount"));
        getUserData.save();
    }

    public void claimReward() {
        PlayerData getUserData = new PlayerData(player.getUniqueId());
        if (getRewardType().equals("ITEM")) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(new ItemMaker().create("", getRewardItem(), null, getRewardAmount()));
                player.sendMessage(Plugin.prefix + "§aReceived " + getRewardAmount() + " " + Plugin.firstLetterCapital(getRewardItem().toString(), true) + "!");
                getUserData.set("Commissions.Completed", getAmountCompleted() + 1);
                getUserData.save();
                createSingle(commNum, null);
            } else player.sendMessage(Plugin.prefix + "§cYou do not have space in your inventory to claim this!");
        } else {
            getUserData.addLand(getRewardAmount(), true);
            player.sendMessage(Plugin.prefix + "§aReceived " + getRewardAmount() + " claim blocks! New Amount: §e" + getUserData.getLandLeft());
            getUserData.set("Commissions.Completed", getAmountCompleted() + 1);
            getUserData.save();
            createSingle(commNum, null);
        }
    }

    boolean commissionAlreadyInUse(String id) {
        PlayerData getUserData = new PlayerData(player.getUniqueId());
        boolean hasCommission0 = (getUserData.contains("Commissions.0"));
        boolean hasCommission1 = (getUserData.contains("Commissions.1"));
        if (!hasCommission0 && !hasCommission1) return false;
        else {
            boolean comm0 = (Objects.equals(getUserData.getString("Commissions.0.Id"), id));
            boolean comm1 = (Objects.equals(getUserData.getString("Commissions.1.Id"), id));
            if (!hasCommission0) comm0 = false;
            if (!hasCommission1) comm1 = false;
            return !(!comm0 && !comm1);
        }
    }

    public boolean isValidId(String id) {
        Set<String> keys = Configs.configs.getCommissions().getConfigurationSection("Commissions").getKeys(false);
        for (String key : keys) if (id.equalsIgnoreCase(key)) return true;
        return false;
    }

    String getRandomCommission() {
        int commAmount = 0;
        Set<String> keys = Configs.configs.getCommissions().getConfigurationSection("Commissions").getKeys(false);
        for (int i = 0; i < keys.size(); i++) commAmount++;
        String randComm = (String) keys.toArray()[new Random().nextInt(commAmount)];
        while (commissionAlreadyInUse(randComm)) randComm = (String) keys.toArray()[new Random().nextInt(commAmount)];
        return randComm;
    }


}
