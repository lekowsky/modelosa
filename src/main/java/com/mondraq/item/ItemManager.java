package com.mondraq.item;

import com.mondraq.main.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class ItemManager {

    public static final String WATERING_CAN  = "watering_can";
    public static final String FERTILIZER    = "fertilizer";
    public static final String PORTABLE_LAMP = "portable_lamp";
    public static final String SPRAYER       = "sprayer";

    private final Main plugin;
    private final NamespacedKey jobKey;
    private final NamespacedKey wandKey;

    private ItemStack pbWateringCan, pbFertilizer, pbPortableLamp, pbSprayer, pbWand;

    public ItemManager(Main plugin) {
        this.plugin = plugin;
        this.jobKey = new NamespacedKey(plugin, "gardener_item");
        this.wandKey = new NamespacedKey(plugin, "selection_wand");
        buildPrototypes();
    }

    public void rebuildPrototypes() {
        buildPrototypes();
    }

    private void buildPrototypes() {
        pbWateringCan = make("watering-can", WATERING_CAN);
        pbFertilizer = make("fertilizer", FERTILIZER);
        pbPortableLamp = make("portable-lamp", PORTABLE_LAMP);
        pbSprayer = make("sprayer", SPRAYER);
        pbWand = buildWand();
    }

    public ItemStack createWateringCan()  { return pbWateringCan.clone(); }
    public ItemStack createFertilizer()   { return pbFertilizer.clone(); }
    public ItemStack createPortableLamp() { return pbPortableLamp.clone(); }
    public ItemStack createSprayer()      { return pbSprayer.clone(); }
    public ItemStack createSelectionWand() { return pbWand.clone(); }

    private ItemStack make(String cfgKey, String val) {
        ItemStack item = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(plugin.getConfigManager().getItemName(cfgKey));
        meta.setLore(plugin.getConfigManager().getItemLore(cfgKey));
        int cmd = plugin.getConfigManager().getItemModelData(cfgKey);
        if (cmd > 0) meta.setCustomModelData(cmd);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(jobKey, PersistentDataType.STRING, val);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildWand() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName("\u00a7e\u00a7lRóżdżka wyboru obszaru");
        meta.setLore(List.of("\u00a77LPM \u00a7f- punkt 1", "\u00a77PPM \u00a7f- punkt 2"));
        meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isJobItem(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(jobKey, PersistentDataType.STRING);
    }

    public String getJobItemId(ItemStack item) {
        if (!isJobItem(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(jobKey, PersistentDataType.STRING);
    }

    public boolean isSelectionWand(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(wandKey, PersistentDataType.BOOLEAN);
    }

    public NamespacedKey getJobItemKey() { return jobKey; }
}
