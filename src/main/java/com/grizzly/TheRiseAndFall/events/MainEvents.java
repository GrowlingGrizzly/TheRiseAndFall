package com.grizzly.TheRiseAndFall.events;


import com.grizzly.TheRiseAndFall.util.Commission;
import com.grizzly.TheRiseAndFall.util.Configs;
import com.grizzly.TheRiseAndFall.util.PlayerData;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class MainEvents implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = new PlayerData(player.getUniqueId());
        playerData.set("Last-Account-Name", player.getName());
        playerData.putIfAbsent("Land-Left", Configs.configs.getConfig().getInt("Defaults.Land"));
        playerData.putIfAbsent("Lives-Left", Configs.configs.getConfig().getInt("Defaults.Lives"));
        playerData.putIfAbsent("Commissions.Completed", 0);
        playerData.putIfAbsent("Ignoring-Claims", false);
        if (playerData.contains("Awaiting-Team-Disband-Msg")) {
            player.sendMessage(Plugin.prefix + "§cWhile you were away your team was disbanded!");
            playerData.set("Awaiting-Team-Disband-Msg", null);
        } playerData.save();
        db.loadInventoryIfBackedUp(player);
        new Commission(player).createDefaults(false);
    }
}
