package com.grizzly.TheRiseAndFall.events;

import com.grizzly.TheRiseAndFall.util.Altar;
import com.grizzly.TheRiseAndFall.util.Claim;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.List;

import static com.grizzly.TheRiseAndFall.util.Database.db;

public class BlockEvents implements Listener {

    Claim claim = new Claim();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPistonExtend(BlockPistonExtendEvent e) { onPistonEvent(e, e.getBlocks(), false); }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPistonRetract(BlockPistonRetractEvent e) { onPistonEvent(e, e.getBlocks(), true); }

    void onPistonEvent(BlockPistonEvent e, List<Block> blocks, boolean isRetract) {
        BlockFace dir = e.getDirection();
        Block pistonBlock = e.getBlock();

        int pistonClaimNum = claim.getClaimAt(pistonBlock.getLocation().getBlockX(), pistonBlock.getLocation().getBlockZ(), pistonBlock.getWorld());
        if (blocks.isEmpty()) {
            if (isRetract) return;
            Block invadedBlock = pistonBlock.getRelative(dir);
            int invadedClaimNum = claim.getClaimAt(invadedBlock.getLocation().getBlockX(), invadedBlock.getLocation().getBlockZ(), invadedBlock.getWorld());
            if (invadedClaimNum != -1 && (pistonClaimNum == -1 || !(pistonClaimNum == invadedClaimNum)) &&
                    !twoClaimsSameOwner(pistonClaimNum, invadedClaimNum)) e.setCancelled(true);
            for (Integer shop : db.getShopIds()) {
                if (db.getShop(shop).location.getBlock().getLocation().equals(invadedBlock.getLocation())) e.setCancelled(true);
            }

        } else for (Block block : blocks) {
            int extraBlockClaimNum = claim.getClaimAt(block.getLocation().getBlockX(), block.getLocation().getBlockZ(), block.getWorld());
            if (isRetract) {
                if (extraBlockClaimNum != -1 && (pistonClaimNum == -1 || !(pistonClaimNum == extraBlockClaimNum)) &&
                        !twoClaimsSameOwner(pistonClaimNum, extraBlockClaimNum)) e.setCancelled(true);
                for (Integer shop : db.getShopIds()) {
                    if (db.getShop(shop).location.getBlock().getLocation().equals(block.getLocation())) e.setCancelled(true);
                }
            } else {
                int blockX = block.getLocation().getBlockX();
                int blockZ = block.getLocation().getBlockZ();
                int newExtraBlockClaimNum = -1;
                switch (dir) {
                    case EAST: newExtraBlockClaimNum = claim.getClaimAt(blockX + 1, blockZ, block.getWorld()); break;
                    case WEST: newExtraBlockClaimNum = claim.getClaimAt(blockX - 1, blockZ, block.getWorld()); break;
                    case NORTH: newExtraBlockClaimNum = claim.getClaimAt(blockX, blockZ - 1, block.getWorld()); break;
                    case SOUTH: newExtraBlockClaimNum = claim.getClaimAt(blockX, blockZ + 1, block.getWorld()); break;
                } if (newExtraBlockClaimNum != -1 && (extraBlockClaimNum == -1 || !(newExtraBlockClaimNum == extraBlockClaimNum)) &&
                        !twoClaimsSameOwner(extraBlockClaimNum, newExtraBlockClaimNum)) e.setCancelled(true);
                for (Integer shop : db.getShopIds()) {
                    if (db.getShop(shop).location.getBlock().getLocation().equals(block.getLocation())) e.setCancelled(true);
                }
            }
        } int altarNum = new Altar().getAltarAt(pistonBlock.getLocation().getBlockX(),
                pistonBlock.getLocation().getBlockY(), pistonBlock.getLocation().getBlockZ());
        if (altarNum != -1) e.setCancelled(true);
    }

    Boolean twoClaimsSameOwner(int claimFrom, int claimTo) {
        String claimFromOwner = new Claim(claimFrom).getOwner();
        String claimToOwner = new Claim(claimTo).getOwner();
        String claimToCoOwner = new Claim(claimTo).getCoOwner();
        return (claimToOwner.equals(claimFromOwner) || claimToCoOwner.equals(claimFromOwner));
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockFromTo(BlockFromToEvent e) {
        if (e.getFace() == BlockFace.DOWN) return;
        Location loc = e.getBlock().getLocation();
        Location toLoc = e.getToBlock().getLocation();
        int fromClaimNum = claim.getClaimAt(loc.getBlockX(), loc.getBlockZ(), toLoc.getWorld());
        int toClaimNum = claim.getClaimAt(toLoc.getBlockX(), toLoc.getBlockZ(), toLoc.getWorld());
        if (toClaimNum != -1 && fromClaimNum != toClaimNum && !twoClaimsSameOwner(fromClaimNum, toClaimNum)) e.setCancelled(true);
        int altarNum = new Altar().getAltarAt(toLoc.getBlockX(), toLoc.getBlockY(), toLoc.getBlockZ());
        if (altarNum != -1) e.setCancelled(true);
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onDispense(BlockDispenseEvent e) {
        Block fromBlock = e.getBlock();
        BlockData fromData = fromBlock.getBlockData();
        if (!(fromData instanceof Dispenser)) return;
        Dispenser dispenser = (Dispenser) fromData;

        Block toBlock = fromBlock.getRelative(dispenser.getFacing());
        int fromClaimNum = claim.getClaimAt(fromBlock.getLocation().getBlockX(), fromBlock.getLocation().getBlockZ(), fromBlock.getWorld());
        int toClaimNum = claim.getClaimAt(toBlock.getLocation().getBlockX(), toBlock.getLocation().getBlockZ(), fromBlock.getWorld());

        int altarNum = new Altar().getAltarAt(toBlock.getLocation().getBlockX(), toBlock.getLocation().getBlockY(), toBlock.getLocation().getBlockZ());
        if (altarNum != -1) e.setCancelled(true);

        if (fromClaimNum == toClaimNum || twoClaimsSameOwner(fromClaimNum, toClaimNum)) return;
        e.setCancelled(true);
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.getEntity().getType() == EntityType.PRIMED_TNT && e.blockList().size() > 0) {
            e.blockList().removeIf(block -> new Claim().getClaimAt(block.getLocation().getBlockX(), block.getLocation().getBlockZ(), block.getWorld()) != -1);
            e.blockList().removeIf(block -> new Altar().getAltarAt(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()) != -1);
            for (Integer shop : db.getShopIds())
                e.blockList().removeIf(block -> db.getShop(shop).location.getBlock().getLocation().equals(block.getLocation()));
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent e) {
        Location rootLocation = e.getLocation();
        Claim rootClaim = new Claim(rootLocation.getBlockX(), rootLocation.getBlockZ(), rootLocation.getWorld());

        for (int i = 0; i < e.getBlocks().size(); i++) {
            BlockState block = e.getBlocks().get(i);
            Claim blockClaim = new Claim(block.getLocation());
            if (blockClaim.isValidClaim())
                if (!rootClaim.isValidClaim() || !rootClaim.getOwner().equals(blockClaim.getOwner())) e.getBlocks().remove(i--);
        } for (int i = 0; i < e.getBlocks().size(); i++) {
            BlockState block = e.getBlocks().get(i);
            int altarNum = new Altar().getAltarAt(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
            if (altarNum != -1) e.getBlocks().remove(i--);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockSpread(BlockSpreadEvent e) {
        Claim sourceClaim = new Claim(e.getSource().getLocation());
        Claim growClaim = new Claim(e.getBlock().getLocation());
        if (growClaim.isValidClaim()) if (!sourceClaim.isValidClaim() || !sourceClaim.getOwner().equals(growClaim.getOwner())) e.setCancelled(true);
        if (new Altar().getAltarAt(e.getBlock().getLocation().getBlockX(),
                e.getBlock().getLocation().getBlockY(), e.getBlock().getLocation().getBlockZ()) != -1) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void chorusFlower(ProjectileHitEvent e) {
        Block block = e.getHitBlock();
        if (block == null || block.getType() != Material.CHORUS_FLOWER) return;
        Claim claim = new Claim(block.getLocation());
        if (claim.getClaimNum() == -1) return;
        Player shooter = null;
        Projectile projectile = e.getEntity();
        if (projectile.getShooter() instanceof Player) shooter = (Player) projectile.getShooter();
        if (shooter == null) {
            e.setCancelled(true);
            return;
        } if (shooter.getName().equals(claim.getOwnerFormatted()) || shooter.getName().equals(claim.getCoOwnerFormatted())) return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemFrameBrokenByBoat(final HangingBreakEvent e) {
        Location loc = e.getEntity().getLocation();
        if (e.getCause() != HangingBreakEvent.RemoveCause.PHYSICS && e.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION) return;
        if (new Claim().getClaimAt(loc.getBlockX(), loc.getBlockZ(), loc.getWorld()) != -1) e.setCancelled(true);
    }


    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e) {
        Block source = e.getSourceBlock();
        Block block = e.getBlock();
        Claim sourceClaim = new Claim(source.getLocation());
        Claim blockClaim = new Claim(block.getLocation());
        if (blockClaim.isValidClaim()) {
            if (sourceClaim.isValidClaim() && blockClaim.getOwner().equals(sourceClaim.getOwner())) return;
            e.setCancelled(true);
        } if (new Altar().getAltarAt(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()) != -1) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.LAVA)
            if (e.getEntity().getName().contains("§a$")) e.setCancelled(true);
        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent e) {
        if (e.getEntity().getName().contains("§a$")) e.setCancelled(true);
    }


}
