package com.mondraq.listener;

import com.mondraq.main.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {

    private final Main plugin;

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (plugin.getJobManager().isWorking(p)) {
            plugin.getJobManager().endJobSilently(p);
        }
        plugin.getAreaManager().clearSelections(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (!plugin.getJobManager().isWorking(p)) return;

        e.getDrops().removeIf(item -> plugin.getItemManager().isJobItem(item));
        plugin.getJobManager().endJobSilently(p);
    }
}
