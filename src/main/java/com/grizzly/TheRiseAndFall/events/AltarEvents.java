package com.grizzly.TheRiseAndFall.events;

import com.grizzly.TheRiseAndFall.util.Altar;
import com.grizzly.TheRiseAndFall.util.Plugin;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.Objects;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class AltarEvents implements Listener {

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e) {
        if (e.getInventory().getLocation() != null && e.getInventory().getType() == InventoryType.CHEST) {
            for (Altar altar : db.currentAltars.values()) {
                if (Objects.equals(altar.inventory.getLocation(), e.getInventory().getLocation())) {
                    e.getPlayer().sendMessage(Plugin.prefix + "§cThis chest is already being used for an altar!");
                    return;
                }
            } if (new Altar((Player) e.getPlayer(), e.getInventory()).hasNoAltar()) new Altar((Player) e.getPlayer(), e.getInventory()).getReadyToCreate(false);
            else new Altar((Player) e.getPlayer(), e.getInventory()).deleteAltarCreation();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onOpenInventory(InventoryOpenEvent e) {
        if (e.getInventory().getLocation() != null && e.getInventory().getType() == InventoryType.CHEST) {
            for (Altar altar : db.currentAltars.values()) {
                if (Objects.equals(altar.inventory.getLocation(), e.getInventory().getLocation())) {
                    e.getPlayer().sendMessage(Plugin.prefix + "§cThis chest is currently being used for an altar!");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getState() instanceof Chest) for (Altar altar : db.currentAltars.values()) {
            if (Objects.equals(altar.inventory.getLocation(), e.getBlock().getLocation())) {
                e.getPlayer().sendMessage(Plugin.prefix + "§cThis chest is currently being used for an altar!");
                e.setCancelled(true);
                return;
            }
        } Location loc = e.getBlock().getLocation();
        Integer alterNum = new Altar().getAltarAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (alterNum != -1 ) if (new Altar(e.getPlayer(), alterNum).hasPermission()) {
            e.setCancelled(true);
            new Altar(e.getPlayer(), alterNum).tearDownAltar(false);
        } else {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Plugin.prefix + "§cYou cannot break this altar!");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Location loc = e.getBlock().getLocation();
        Integer alterNum = new Altar().getAltarAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (alterNum != -1) {
            e.getPlayer().sendMessage(Plugin.prefix + "§cYou cannot build inside an altar!");
            e.setCancelled(true);
        }
    }

}
