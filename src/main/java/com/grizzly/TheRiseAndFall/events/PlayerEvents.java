package com.grizzly.TheRiseAndFall.events;

import com.grizzly.TheRiseAndFall.commands.CmdClaim;
import com.grizzly.TheRiseAndFall.util.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class PlayerEvents implements Listener {

    HashMap<UUID, Double> oldTick = new HashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onOpenInventory(InventoryOpenEvent e) {
        if (e.getInventory().getLocation() != null) {
            int claimNum = new Claim().getClaimAt(e.getPlayer().getLocation().getBlockX(), e.getPlayer().getLocation().getBlockZ(), e.getPlayer().getWorld());
            if (claimNum != -1 && !(new Claim(claimNum).hasPermissions((Player) e.getPlayer()))) {
                noInteract((Player) e.getPlayer(), claimNum);
                e.setCancelled(true);
                return;
            } if (e.getInventory().getType().equals(InventoryType.CHEST) && Configs.configs.getConfig().getBoolean("Shops.Enabled")) {
                if (e.getInventory().getSize() == 27 && e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.STICK)) {
                    ItemStack[] emptyChest = new ItemStack[]{null, null, null, null, null, null, null, null, null, null, null,
                            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,};
                    if (Arrays.equals(e.getInventory().getContents(), emptyChest)) {
                        new Shop((Player) e.getPlayer(), -1).startCreation(e.getInventory());
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        int claimNum = new Claim().getClaimAt(e.getBlock().getLocation().getBlockX(), e.getBlock().getLocation().getBlockZ(), e.getBlock().getWorld());
        if (claimNum != -1 && !(new Claim(claimNum).hasPermissions(e.getPlayer()))) {
            noInteract(e.getPlayer(), claimNum);
            e.setCancelled(true);
        } for (Integer shop : db.getShopIds()) {
            if (db.getShop(shop).location.getBlock().getLocation().equals(e.getBlock().getLocation())) e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        int claimNum = new Claim().getClaimAt(e.getBlock().getLocation().getBlockX(), e.getBlock().getLocation().getBlockZ(), e.getBlock().getWorld());
        if (claimNum != -1 && !(new Claim(claimNum).hasPermissions(e.getPlayer()))) {
            noInteract(e.getPlayer(), claimNum);
            e.setCancelled(true);
        } for (Integer shop : db.getShopIds()) {
            if (db.getShop(shop).location.getBlock().getLocation().equals(e.getBlock().getLocation())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (db.inventoryBackedUp(player)) {
            Configs config = Configs.configs;
            @SuppressWarnings("unchecked") List<ItemStack> itemList = (List<ItemStack>) config.getInvBackup().get(player.getUniqueId().toString());
            e.getDrops().clear();
            e.getDrops().addAll(itemList);
            config.getInvBackup().set(player.getUniqueId().toString(), null);
            config.saveInvBackup();
            player.setCanPickupItems(true);
        } if (player.getKiller() != null)
            if (Configs.configs.getConfig().getBoolean("Lives.Enabled"))
                new PlayerData(player.getUniqueId()).setLivesLeft(new PlayerData(player.getUniqueId()).getLivesLeft());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        double currentTick = LocalDateTime.now().getNano()/1000000.0/50.0;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            int claimNum = new Claim().getClaimAt(e.getClickedBlock().getLocation().getBlockX(), e.getClickedBlock().getLocation().getBlockZ(), e.getClickedBlock().getWorld());
            if (claimNum != -1 && !(new Claim(claimNum).hasPermissions(e.getPlayer()))) {
                if (!oldTick.containsKey(player.getUniqueId()) || currentTick - oldTick.get(player.getUniqueId()) > 5
                        || currentTick - oldTick.get(player.getUniqueId()) < -5) noInteract(player, claimNum);
                oldTick.put(player.getUniqueId(), currentTick);
                e.setCancelled(true);
            } int altarNum = new Altar().getAltarAt(e.getClickedBlock().getLocation().getBlockX(),
                    e.getClickedBlock().getLocation().getBlockY(), e.getClickedBlock().getLocation().getBlockZ());
            if (altarNum != -1) e.setCancelled(true);
        }
     }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBrokenEntity(HangingBreakByEntityEvent e) {
        Location loc = e.getEntity().getLocation();
        if (!(e.getRemover() instanceof Player)) {
            e.setCancelled(true);
            return;
        } Player player = (Player) e.getRemover();
        int claimNum = new Claim().getClaimAt(loc.getBlockX(), loc.getBlockZ(), loc.getWorld());
        if (claimNum != -1 && !(new Claim(claimNum).hasPermissions(player))) {
            noInteract(player, new Claim(loc).getClaimNum());
            e.setCancelled(true);
        }
        int altarNum = new Altar().getAltarAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (altarNum != -1) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked().getType() == EntityType.ARMOR_STAND) this.onPlayerInteractEntity(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        for (Integer shop : db.getShopIds())  if (e.getRightClicked().getCustomName() != null && e.getRightClicked().getCustomName().contains("§c" + shop)) {
            if (Configs.configs.getConfig().getBoolean("Shops.Enabled")) new Shop(player, shop).open();
            else player.sendMessage(Plugin.prefix + "§cShops are currently disabled!");
            e.setCancelled(true);
            return;
        } double currentTick = LocalDateTime.now().getNano()/1000000.0/50.0;
        if (new Claim(e.getRightClicked().getLocation()).isValidClaim()) {
            if (!new Claim(e.getRightClicked().getLocation()).hasPermissions(player)) {
                e.setCancelled(true);
                if (!oldTick.containsKey(player.getUniqueId()) || currentTick - oldTick.get(player.getUniqueId()) > 5
                        || currentTick - oldTick.get(player.getUniqueId()) < -5)
                    noInteract(player, new Claim(e.getRightClicked().getLocation()).getClaimNum());
                oldTick.put(player.getUniqueId(), currentTick);
            }
        } int altarNum = new Altar().getAltarAt(e.getRightClicked().getLocation().getBlockX(),
                e.getRightClicked().getLocation().getBlockY(), e.getRightClicked().getLocation().getBlockZ());
        if (altarNum != -1) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamageShop(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        for (Integer shop : db.getShopIds())  if (e.getEntity().getCustomName() != null && e.getEntity().getCustomName().contains("§c" + shop)) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDropItem(PlayerDropItemEvent e) {
        if (new Claim(e.getItemDrop().getLocation()).isValidClaim()) {
            if (new Claim(e.getItemDrop().getLocation()).hasPermissions(e.getPlayer())) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if (new Claim(e.getItem().getLocation()).isValidClaim()) {
            if (!new Claim(e.getItem().getLocation()).hasPermissions(player))
            e.setCancelled(true);
        } if (e.getItem().getName().contains("§a$")) e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            int claimFrom = new Claim().getClaimAt(e.getFrom().getBlockX(), e.getFrom().getBlockZ(), e.getTo().getWorld());
            int claimTo = new Claim().getClaimAt(e.getTo().getBlockX(), e.getTo().getBlockZ(), e.getTo().getWorld());
            if (claimFrom != claimTo && claimTo != -1) {
                Claim claim = new Claim(claimTo);
                if (claimFrom != -1) {
                    if (claim.getOwner().equalsIgnoreCase(new Claim(claimFrom).getOwner())) return;
                } e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6Now entering §r" + new Team().getName(claim.getOwner()) + "§6's claim."));
                return;
            } if (claimFrom != claimTo && claimFrom != -1) {
                Claim claim = new Claim(claimFrom);
                if (claimTo != -1) {
                    if (claim.getOwner().equalsIgnoreCase(new Claim(claimTo).getOwner())) return;
                } e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6Now leaving §r" + new Team().getName(claim.getOwner()) + "§6's claim."));
            }
        }
    }

    void noInteract(Player player, int claimNum) {
        player.sendMessage("§cYou cannot interact with §r" + new Team().getName(new Claim(claimNum).getOwner()) + "§c's claims!");
        new CmdClaim().showOtherClaimParticles(player, claimNum, true);
    }

}
