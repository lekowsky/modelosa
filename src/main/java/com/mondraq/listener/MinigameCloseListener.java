package com.mondraq.listener;

import com.mondraq.gui.MinigameManager;
import com.mondraq.gui.MinigameSession;
import com.mondraq.main.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class MinigameCloseListener implements Listener {

    private final Main plugin;

    public MinigameCloseListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(MinigameManager.MINIGAME_TITLE)) return;

        MinigameSession session = plugin.getMinigameManager().getSession(p);
        if (session != null) {
            plugin.getMinigameManager().abortMinigame(p);
        }
    }
}
