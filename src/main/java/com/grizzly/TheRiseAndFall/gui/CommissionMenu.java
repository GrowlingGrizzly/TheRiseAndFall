package com.grizzly.TheRiseAndFall.gui;

import com.grizzly.TheRiseAndFall.util.Commission;
import com.grizzly.TheRiseAndFall.util.ItemMaker;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommissionMenu implements InventoryHolder {

    final Player player;

    public CommissionMenu(Player player) { this.player = player; }

    public void open() {
        player.openInventory(getInventory());
        update();
    }

    public void update() {
        Commission comm0 = new Commission(player, 0);
        Commission comm1 = new Commission(player, 1);
        Inventory inv = player.getOpenInventory().getTopInventory();
        for (int i = 0; i < 27; i++) inv.setItem(i, new ItemMaker().createBlank());
        inv.setItem(12, new ItemMaker().create("§b" + comm0.getName(), Material.BOOK, commissionLore(comm0)));
        inv.setItem(14, new ItemMaker().create("§b" + comm1.getName(), Material.BOOK, commissionLore(comm1)));
        inv.setItem(18, new ItemMaker().create("§b§lReroll commissions", Material.PAPER, Arrays.asList("§7Reroll any commissions you have", "§7not put items into!", "§7Cost: 1 Diamond")));
    }

    List<String> commissionLore(Commission comm) {
        String reward;
        try { reward = Plugin.firstLetterCapital(comm.getRewardItem().toString(), true);
        } catch (Exception e) { reward = "Claim Land"; }
        List<String> list = new ArrayList<>(Arrays.asList("", "§7Submit " + comm.getAmount() + " " + Plugin.firstLetterCapital(comm.getItem().toString(), true),
                "", "§3Progress: §a" + (comm.getProgress()*100) / comm.getAmount() + "% (" + comm.getProgress() + "/" + comm.getAmount() + ")", "",
                "§3Reward: §a" + comm.getRewardAmount() + " " + reward, ""));
        if ((comm.getProgress()*100) / comm.getAmount() == 100) list.add("§aClick to claim rewards");
        else list.add("§aClick to submit items");
        return list;
    }

    Inventory createInventory() { return Bukkit.createInventory(this, 27, "§3§lCommissions"); }

    @Override
    public Inventory getInventory() { return createInventory(); }

}
