package com.mondraq.main;

import com.mondraq.area.AreaManager;
import com.mondraq.command.GardenerCommand;
import com.mondraq.config.ConfigManager;
import com.mondraq.gui.MinigameManager;
import com.mondraq.item.ItemManager;
import com.mondraq.job.JobManager;
import com.mondraq.listener.*;
import com.mondraq.npc.GuiManager;
import com.mondraq.npc.NpcManager;
import com.mondraq.task.GlowParticleTask;
import com.mondraq.task.PlantSicknessTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class Main extends JavaPlugin {

    private static Main instance;
    private ConfigManager configManager;
    private ItemManager itemManager;
    private AreaManager areaManager;
    private NpcManager npcManager;
    private GuiManager guiManager;
    private JobManager jobManager;
    private MinigameManager minigameManager;
    private Economy economy;
    private BukkitTask sicknessTask;
    private BukkitTask particleTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        itemManager   = new ItemManager(this);
        areaManager   = new AreaManager(this);
        npcManager    = new NpcManager(this);
        guiManager    = new GuiManager(this);
        jobManager    = new JobManager(this);
        minigameManager = new MinigameManager(this);

        setupVault();
        registerListeners();
        registerCommands();
        startTasks();

        getLogger().info("Plugin uruchomiony.");
    }

    @Override
    public void onDisable() {
        if (jobManager != null) jobManager.endAllSessions();
        getLogger().info("Plugin wylaczony.");
    }

    public void fullReload() {
        configManager.reload();
        areaManager.reload();
        itemManager.rebuildPrototypes();
        cancelTasks();
        startTasks();
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault nie znaleziony - nagrody wylaczone.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        } else {
            getLogger().warning("Brak pluginu ekonomii - nagrody wylaczone.");
        }
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new NpcListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new TeleportListener(this), this);
        pm.registerEvents(new HealingListener(this), this);
        pm.registerEvents(new MinigameCloseListener(this), this);
    }

    private void registerCommands() {
        var cmd = getCommand("gardener");
        if (cmd != null) {
            GardenerCommand handler = new GardenerCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
    }

    private void startTasks() {
        long interval = configManager.getSicknessIntervalSeconds() * 20L;
        sicknessTask = new PlantSicknessTask(this).runTaskTimer(this, interval, interval);
        particleTask = new GlowParticleTask(this).runTaskTimer(this, 10L, 10L);
    }

    private void cancelTasks() {
        if (sicknessTask != null && !sicknessTask.isCancelled()) sicknessTask.cancel();
        if (particleTask != null && !particleTask.isCancelled()) particleTask.cancel();
    }

    public static Main getInstance()         { return instance; }
    public ConfigManager getConfigManager()  { return configManager; }
    public ItemManager getItemManager()      { return itemManager; }
    public AreaManager getAreaManager()       { return areaManager; }
    public NpcManager getNpcManager()        { return npcManager; }
    public GuiManager getGuiManager()        { return guiManager; }
    public JobManager getJobManager()        { return jobManager; }
    public MinigameManager getMinigameManager() { return minigameManager; }
    public Economy getEconomy()              { return economy; }
}
