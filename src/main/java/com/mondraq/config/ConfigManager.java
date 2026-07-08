package com.mondraq.config;

import com.mondraq.main.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.logging.Logger;

public final class ConfigManager {

    private final Main plugin;
    private final Logger log;
    private FileConfiguration cfg;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        cfg = plugin.getConfig();
        validate();
    }

    private void validate() {
        clamp("job.sickness-interval-seconds", getSicknessIntervalSeconds(), 5);
        clamp("job.sick-plants-per-cycle", getSickPlantsPerCycle(), 1);
        clamp("job.max-sick-plants", getMaxSickPlants(), 1);
        clamp("job.inspection-duration-seconds", getInspectionDurationSeconds(), 1);
        clamp("job.dead-bush-cooldown-seconds", getDeadBushCooldownSeconds(), 5);
        if (cfg.getDouble("job.reward-per-plant", 10.0) < 0)
            log.warning("[Config] job.reward-per-plant ujemna — ustawiono 0.0");
    }

    private void clamp(String path, int actual, int min) {
        if (cfg.getInt(path, actual) < min)
            log.warning("[Config] " + path + " za niska — ustawiono minimum " + min);
    }

    public int getSicknessIntervalSeconds()  { return Math.max(5,  cfg.getInt("job.sickness-interval-seconds", 15)); }
    public int getSickPlantsPerCycle()        { return Math.max(1,  cfg.getInt("job.sick-plants-per-cycle", 3)); }
    public int getMaxSickPlants()            { return Math.min(50, Math.max(1, cfg.getInt("job.max-sick-plants", 10))); }
    public int getInspectionDurationSeconds(){ return Math.max(1,  cfg.getInt("job.inspection-duration-seconds", 5)); }
    public int getDeadBushCooldownSeconds()  { return Math.max(5,  cfg.getInt("job.dead-bush-cooldown-seconds", 300)); }
    public double getRewardPerPlant()        { return Math.max(0.0, cfg.getDouble("job.reward-per-plant", 10.0)); }

    public String getMessage(String key) {
        return color(cfg.getString("messages." + key, "&cBrak: " + key));
    }

    public String getMessage(String key, String... pairs) {
        String msg = getMessage(key);
        for (int i = 0; i + 1 < pairs.length; i += 2)
            msg = msg.replace(pairs[i], pairs[i + 1]);
        return msg;
    }

    public String getItemName(String key) {
        return color(cfg.getString("items." + key + ".name", "&fItem"));
    }

    public List<String> getItemLore(String key) {
        List<String> lore = cfg.getStringList("items." + key + ".lore");
        lore.replaceAll(this::color);
        return lore;
    }

    public int getItemModelData(String key) {
        return cfg.getInt("items." + key + ".model-data", 0);
    }

    private String color(String s) {
        return s == null ? "" : s.replace("&", "\u00a7");
    }
}
