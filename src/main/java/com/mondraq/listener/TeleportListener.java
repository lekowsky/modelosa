package com.mondraq.listener;

import com.mondraq.main.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class TeleportListener implements Listener {

    private final Main plugin;

    public TeleportListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent event) {
        Player p = event.getPlayer();
        if (!plugin.getJobManager().isWorking(p)) return;

        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.COMMAND
                || cause == PlayerTeleportEvent.TeleportCause.PLUGIN
                || cause == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true);
            p.sendMessage(plugin.getConfigManager().getMessage("cannot-teleport"));
        }
    }
}
