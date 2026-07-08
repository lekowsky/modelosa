package com.mondraq.listener;

import com.mondraq.gui.MinigameManager;
import com.mondraq.gui.MinigameSession;
import com.mondraq.main.Main;
import com.mondraq.npc.GuiManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public final class InventoryListener implements Listener {

    private final Main plugin;

    public InventoryListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (e.getView().getTitle().equals(GuiManager.GUI_TITLE)) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;

            Material type = e.getCurrentItem().getType();
            if (type == Material.LIME_DYE) {
                p.closeInventory();
                String ghName = plugin.getGuiManager().getViewingJob(p);
                if (ghName != null) {
                    plugin.getJobManager().startJob(p, ghName);
                }
            } else if (type == Material.RED_DYE) {
                p.closeInventory();
                plugin.getJobManager().endJob(p);
            }
            return;
        }

        if (e.getView().getTitle().equals(MinigameManager.MINIGAME_TITLE)) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            
            Material type = e.getCurrentItem().getType();
            if (type == Material.RED_STAINED_GLASS_PANE) {
                MinigameSession minigame = plugin.getMinigameManager().getSession(p);
                if (minigame == null) return;
                
                ItemStack green = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                org.bukkit.inventory.meta.ItemMeta meta = green.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("\u00a7aᴜʟᴇᴄᴢᴏɴᴇ ᴍɪᴇᴊsᴄᴇ");
                    green.setItemMeta(meta);
                }
                e.getInventory().setItem(e.getRawSlot(), green);
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                
                minigame.decrementSpots();
                if (minigame.getRemainingSpots() <= 0) {
                    plugin.getMinigameManager().completeHeal(p);
                    p.closeInventory();
                }
            } else if (type == Material.GRAY_STAINED_GLASS_PANE) {
                MinigameSession minigame = plugin.getMinigameManager().getSession(p);
                if (minigame != null) {
                    plugin.getMinigameManager().failMinigame(p);
                    p.closeInventory();
                }
            }
            return;
        }

        if (!plugin.getJobManager().isWorking(p)) return;

        ItemStack cur = e.getCursor();
        ItemStack slot = e.getCurrentItem();

        if (e.getView().getTopInventory().getType() != InventoryType.CRAFTING) {
            if (plugin.getItemManager().isJobItem(cur) || plugin.getItemManager().isJobItem(slot)) {
                deny(e, p);
                return;
            }
        }

        if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && plugin.getItemManager().isJobItem(slot)) {
            deny(e, p);
            return;
        }

        if (e.getAction() == InventoryAction.HOTBAR_SWAP) {
            int hb = e.getHotbarButton();
            if (hb >= 0) {
                ItemStack hbi = p.getInventory().getItem(hb);
                if (plugin.getItemManager().isJobItem(hbi) || plugin.getItemManager().isJobItem(slot)) {
                    deny(e, p);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (e.getView().getTitle().equals(GuiManager.GUI_TITLE) || e.getView().getTitle().equals(MinigameManager.MINIGAME_TITLE)) {
            e.setCancelled(true);
            return;
        }

        if (!plugin.getJobManager().isWorking(p)) return;

        if (plugin.getItemManager().isJobItem(e.getOldCursor())) deny(e, p);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getJobManager().isWorking(p)) return;
        if (plugin.getItemManager().isJobItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
            p.sendMessage(plugin.getConfigManager().getMessage("cannot-drop-items"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getJobManager().isWorking(p)) return;
        if (plugin.getItemManager().isJobItem(e.getMainHandItem()) || plugin.getItemManager().isJobItem(e.getOffHandItem())) {
            deny(e, p);
        }
    }

    private void deny(org.bukkit.event.Cancellable e, Player p) {
        e.setCancelled(true);
        p.sendMessage(plugin.getConfigManager().getMessage("cannot-move-items"));
    }
}
