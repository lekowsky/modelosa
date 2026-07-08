package com.mondraq.listener;

import com.mondraq.main.Main;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class NpcListener implements Listener {

    private final Main plugin;

    public NpcListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent e) {
        if (!plugin.getNpcManager().isNpc(e.getNPC())) return;
        
        Player p = e.getClicker();
        String ghName = plugin.getNpcManager().getGreenhouseName(e.getNPC());
        
        if (ghName != null) {
            plugin.getGuiManager().openJobGui(p, ghName);
        } else {
            p.sendMessage(plugin.getConfigManager().getMessage("no-area-set"));
        }
    }
}
