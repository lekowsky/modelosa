package com.mondraq.gui;

import com.mondraq.job.JobSession;
import com.mondraq.main.Main;
import com.mondraq.packet.PacketHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class MinigameManager {

    private final Main plugin;
    public static final String MINIGAME_TITLE = "\u00a72\u00a7lʟᴇᴄᴢᴇɴɪᴇ ʀᴏsʟɪɴʏ";
    private final Map<UUID, MinigameSession> activeSessions = new ConcurrentHashMap<>();

    public MinigameManager(Main plugin) {
        this.plugin = plugin;
    }

    public void startMinigame(Player p, Location loc) {
        if (activeSessions.containsKey(p.getUniqueId())) return;

        Inventory inv = Bukkit.createInventory(null, 27, MINIGAME_TITLE);

        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName(" ");
            bg.setItemMeta(bgMeta);
        }

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, bg);
        }

        int spots = ThreadLocalRandom.current().nextInt(5, 9);
        activeSessions.put(p.getUniqueId(), new MinigameSession(loc, spots));

        ItemStack inf = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta infMeta = inf.getItemMeta();
        if (infMeta != null) {
            infMeta.setDisplayName("\u00a7cᴢᴀɪɴғᴇᴋᴏᴡᴀɴᴇ ᴍɪᴇᴊsᴄᴇ");
            infMeta.setLore(Collections.singletonList("\u00a77ᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴜʟᴇᴄᴢʏᴄ"));
            inf.setItemMeta(infMeta);
        }

        int placed = 0;
        while (placed < spots) {
            int slot = ThreadLocalRandom.current().nextInt(27);
            ItemStack cur = inv.getItem(slot);
            if (cur != null && cur.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                inv.setItem(slot, inf);
                placed++;
            }
        }

        p.openInventory(inv);
    }

    public MinigameSession getSession(Player p) {
        return activeSessions.get(p.getUniqueId());
    }

    public void completeHeal(Player p) {
        MinigameSession ms = activeSessions.remove(p.getUniqueId());
        if (ms == null) return;
        
        Location loc = ms.getPlantLoc();
        JobSession s = plugin.getJobManager().getSession(p);
        if (s == null) return;

        PacketHelper.restoreBlock(p, loc);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) PacketHelper.restoreBlock(p, loc);
        }, 1L);
        
        s.removeSickPlant(loc);
        s.incrementHealed();
        p.sendMessage(plugin.getConfigManager().getMessage("plant-healed"));
        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 0.8f, 1.2f);
        plugin.getJobManager().updateBossBar(p);
    }

    public void abortMinigame(Player p) {
        MinigameSession ms = activeSessions.remove(p.getUniqueId());
        if (ms != null) {
            p.sendMessage("\u00a7cᴘʀᴢᴇʀᴡᴀɴᴏ ʟᴇᴄᴢᴇɴɪᴇ ʀᴏsʟɪɴʏ.");
            Location loc = ms.getPlantLoc();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (p.isOnline() && plugin.getJobManager().isWorking(p)) {
                    PacketHelper.sendFakeBlock(p, loc, PacketHelper.SICK_BLOCK);
                }
            }, 2L);
        }
    }

    public void failMinigame(Player p) {
        MinigameSession ms = activeSessions.remove(p.getUniqueId());
        if (ms == null) return;

        Location loc = ms.getPlantLoc();
        JobSession s = plugin.getJobManager().getSession(p);
        if (s == null) return;

        com.mondraq.job.SickPlantData d = s.getSickPlant(loc);
        if (d != null) {
            d.setState(com.mondraq.job.SickPlantData.State.DEAD);
        }

        PacketHelper.sendFakeBlock(p, loc, Material.DEAD_BUSH);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) PacketHelper.sendFakeBlock(p, loc, Material.DEAD_BUSH);
        }, 1L);

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
