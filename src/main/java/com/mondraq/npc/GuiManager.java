package com.mondraq.npc;

import com.mondraq.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public final class GuiManager {

    private final Main plugin;
    public static final String GUI_TITLE = "\u00a72\u00a7lᴘʀᴀᴄᴀ: ᴏɢʀᴏᴅɴɪᴋ";
    
    private final java.util.Map<java.util.UUID, String> viewingJob = new java.util.HashMap<>();

    public GuiManager(Main plugin) {
        this.plugin = plugin;
    }

    public void openJobGui(Player p, String ghName) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        boolean isWorking = plugin.getJobManager().isWorking(p);

        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName(" ");
            background.setItemMeta(bgMeta);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, background);
        }

        ItemStack start = new ItemStack(Material.LIME_DYE);
        ItemMeta startMeta = start.getItemMeta();
        if (startMeta != null) {
            startMeta.setDisplayName("\u00a7a\u00a7lʀᴏᴢᴘᴏᴄᴢɴɪᴊ ᴘʀᴀᴄᴇ");
            startMeta.setLore(Collections.singletonList("\u00a77ᴋʟɪᴋɴɪᴊ, ᴀʙʏ ʀᴏᴢᴘᴏᴄᴢᴀᴄ ᴢᴍɪᴀɴᴇ ᴡ: \u00a7e" + ghName));
            start.setItemMeta(startMeta);
        }

        ItemStack end = new ItemStack(Material.RED_DYE);
        ItemMeta endMeta = end.getItemMeta();
        if (endMeta != null) {
            endMeta.setDisplayName("\u00a7c\u00a7lᴢᴀᴋᴏɴᴄᴢ ᴘʀᴀᴄᴇ");
            endMeta.setLore(Collections.singletonList("\u00a77ᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴢᴀᴋᴏɴᴄᴢʏᴄ ᴏʙᴇᴄɴᴀ ᴢᴍɪᴀɴᴇ"));
            end.setItemMeta(endMeta);
        }

        if (isWorking) {
            inv.setItem(13, end);
        } else {
            inv.setItem(13, start);
        }

        viewingJob.put(p.getUniqueId(), ghName);
        p.openInventory(inv);
    }
    
    public String getViewingJob(Player p) {
        return viewingJob.get(p.getUniqueId());
    }
    
    public void clearViewingJob(Player p) {
        viewingJob.remove(p.getUniqueId());
    }
}
