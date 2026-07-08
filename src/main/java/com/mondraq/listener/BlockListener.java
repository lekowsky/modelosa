package com.mondraq.listener;

import com.mondraq.main.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockListener implements Listener {

    private final Main plugin;

    public BlockListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getJobManager().isWorking(p)) {
            if (plugin.getAreaManager().containsAny(e.getBlock().getLocation())) {
                plugin.getAreaManager().invalidateCache(e.getBlock().getLocation());
            }
            return;
        }
        if (plugin.getAreaManager().containsAny(e.getBlock().getLocation())) {
            e.setCancelled(true);
            p.sendMessage(plugin.getConfigManager().getMessage("cannot-break-blocks"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getJobManager().isWorking(p)) {
            if (plugin.getAreaManager().containsAny(e.getBlock().getLocation())) {
                plugin.getAreaManager().invalidateCache(e.getBlock().getLocation());
            }
            return;
        }
        if (plugin.getAreaManager().containsAny(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }
}
