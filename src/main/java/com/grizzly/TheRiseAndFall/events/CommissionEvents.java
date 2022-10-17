package com.grizzly.TheRiseAndFall.events;

import com.grizzly.TheRiseAndFall.gui.CommissionMenu;
import com.grizzly.TheRiseAndFall.util.Commission;
import com.grizzly.TheRiseAndFall.util.ItemMaker;
import com.grizzly.TheRiseAndFall.util.PlayerData;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CommissionEvents implements Listener {

    @EventHandler
    public void onClickItem(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (player.getOpenInventory().getTitle().equals("§3§lCommissions")) {
            if (e.getRawSlot() == 12) clickCommission(player, 0);
            else if (e.getRawSlot() == 14) clickCommission(player, 1);
            else if (e.getRawSlot() == 18) {
                if (player.getInventory().contains(Material.DIAMOND, 1)) {
                    Commission comm0 = new Commission(player, 0);
                    Commission comm1 = new Commission(player, 1);
                    if (comm0.getProgress() > 0 && comm1.getProgress() > 0) player.sendMessage(
                            Plugin.prefix + "§cYou do not have any commissions you can reroll!");
                    else {
                        player.getInventory().removeItem(new ItemMaker().create("", Material.DIAMOND, null));
                        if (!(comm0.getProgress() > 0)) comm0.createSingle(0, null);
                        if (!(comm1.getProgress() > 0)) comm1.createSingle(1, null);
                        new CommissionMenu(player).update();
                        player.sendMessage(Plugin.prefix + "§aCommissions rerolled!");
                    }
                } else player.sendMessage(Plugin.prefix + "§cYou do not have a diamond!");
            } e.setCancelled(true);
        }
    }

    public void clickCommission(Player player, Integer commNum) {
        PlayerData getUserData = new PlayerData(player.getUniqueId());
        Commission comm = new Commission(player, commNum);
        if ((comm.getProgress()*100) / comm.getAmount() == 100) {
            comm.claimReward();
        } else {
            int amountNeeded = comm.getAmount() - comm.getProgress();
            for (int i = 0; i < player.getInventory().getStorageContents().length; i++) {
                ItemStack[] items = player.getInventory().getStorageContents();
                if (items[i] != null && items[i].getType() == comm.getItem()) {
                    int amount = items[i].getAmount();
                    if (amountNeeded - amount >= 0) {
                        items[i].setAmount(0);
                        amountNeeded -= amount;
                    } else {
                        int amountToKeep = (amountNeeded - amount) * -1;
                        items[i].setAmount(amountToKeep);
                        amountNeeded = 0;
                    }
                } player.getInventory().setStorageContents(items);
            } getUserData.set("Commissions." + commNum + ".Progress", comm.getAmount() - amountNeeded);
            getUserData.save();
        } new CommissionMenu(player).update();
    }
}
