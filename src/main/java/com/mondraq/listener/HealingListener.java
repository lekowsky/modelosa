package com.mondraq.listener;

import com.mondraq.job.Ailment;
import com.mondraq.job.JobSession;
import com.mondraq.job.SickPlantData;
import com.mondraq.main.Main;
import com.mondraq.packet.PacketHelper;
import com.mondraq.task.InspectionTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class HealingListener implements Listener {

    private final Main plugin;

    public HealingListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        Block b = e.getClickedBlock();
        if (b == null) return;
        Location loc = b.getLocation();

        ItemStack held = p.getInventory().getItemInMainHand();
        if (plugin.getItemManager().isSelectionWand(held)) return;

        if (!plugin.getJobManager().isWorking(p)) return;
        JobSession s = plugin.getJobManager().getSession(p);

        if (s.isExcluded(loc)) {
            e.setCancelled(true);
            p.sendMessage(plugin.getConfigManager().getMessage("plant-is-dead"));
            syncVisual(p, loc, Material.DEAD_BUSH);
            return;
        }

        if (!s.isSickPlant(loc)) return;
        e.setCancelled(true);

        SickPlantData d = s.getSickPlant(loc);

        if (d.getState() == SickPlantData.State.DEAD) {
            p.sendMessage(plugin.getConfigManager().getMessage("plant-is-dead"));
            syncVisual(p, loc, Material.DEAD_BUSH);
            return;
        }

        if (d.getState() == SickPlantData.State.BEING_INSPECTED) {
            p.sendMessage(plugin.getConfigManager().getMessage("plant-already-inspected"));
            syncVisual(p, loc, PacketHelper.SICK_BLOCK);
            return;
        }

        if (held.getType().isAir()) {
            if (d.getState() == SickPlantData.State.DIAGNOSED) {
                p.sendMessage(plugin.getConfigManager().getMessage(d.getAilment().getMessageKey()));
                syncVisual(p, loc, PacketHelper.SICK_BLOCK);
                return;
            }
            if (d.getState() != SickPlantData.State.SICK) {
                p.sendMessage(plugin.getConfigManager().getMessage("plant-already-inspected"));
                syncVisual(p, loc, PacketHelper.SICK_BLOCK);
                return;
            }
            if (s.isInspecting()) return;

            d.setState(SickPlantData.State.BEING_INSPECTED);
            s.setInspecting(true);
            s.setInspectLocKey(JobSession.key(loc));
            p.sendMessage(plugin.getConfigManager().getMessage("inspection-start"));
            
            syncVisual(p, loc, PacketHelper.SICK_BLOCK);
            new InspectionTask(plugin, p, loc).runTaskTimer(plugin, 20L, 20L);
            return;
        }

        if (!plugin.getItemManager().isJobItem(held)) {
            syncVisual(p, loc, PacketHelper.SICK_BLOCK);
            return;
        }

        if (d.getState() != SickPlantData.State.DIAGNOSED) {
            p.sendMessage(plugin.getConfigManager().getMessage("must-inspect-first"));
            syncVisual(p, loc, PacketHelper.SICK_BLOCK);
            return;
        }

        String id = plugin.getItemManager().getJobItemId(held);
        Ailment a = d.getAilment();

        if (a.getToolId().equals(id)) {
            plugin.getMinigameManager().startMinigame(p, loc);
        } else {
            kill(s, d, loc, p);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeftWand(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        if (!plugin.getItemManager().isSelectionWand(p.getInventory().getItemInMainHand())) return;

        Block b = e.getClickedBlock();
        if (b == null) return;

        e.setCancelled(true);
        plugin.getAreaManager().setPos1(p.getUniqueId(), b.getLocation());
        p.sendMessage(plugin.getConfigManager().getMessage("pos1-set",
                "{x}", String.valueOf(b.getX()), "{y}", String.valueOf(b.getY()), "{z}", String.valueOf(b.getZ())));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightWand(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        if (!plugin.getItemManager().isSelectionWand(p.getInventory().getItemInMainHand())) return;

        Block b = e.getClickedBlock();
        if (b == null) return;

        e.setCancelled(true);
        plugin.getAreaManager().setPos2(p.getUniqueId(), b.getLocation());
        p.sendMessage(plugin.getConfigManager().getMessage("pos2-set",
                "{x}", String.valueOf(b.getX()), "{y}", String.valueOf(b.getY()), "{z}", String.valueOf(b.getZ())));
    }

    private void syncVisual(Player p, Location loc, Material mat) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) PacketHelper.sendFakeBlock(p, loc, mat);
        }, 1L);
    }

    private void kill(JobSession s, SickPlantData d, Location loc, Player p) {
        d.setState(SickPlantData.State.DEAD);
        PacketHelper.sendFakeBlock(p, loc, Material.DEAD_BUSH);
        syncVisual(p, loc, Material.DEAD_BUSH);

        s.removeSickPlant(loc);
        s.excludeLocation(loc);
        s.incrementKilled();
        p.sendMessage(plugin.getConfigManager().getMessage("plant-killed"));
        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 0.8f);
        plugin.getJobManager().updateBossBar(p);

        long cd = plugin.getConfigManager().getDeadBushCooldownSeconds() * 20L;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline() && plugin.getJobManager().isWorking(p)) {
                PacketHelper.restoreBlock(p, loc);
            }
            s.unexcludeLocation(loc);
        }, cd);
    }
}
