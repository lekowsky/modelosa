package com.mondraq.job;

import com.mondraq.area.Greenhouse;
import com.mondraq.main.Main;
import com.mondraq.packet.PacketHelper;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class JobManager {

    private final Main plugin;
    private final Map<UUID, JobSession> sessions = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public JobManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean startJob(Player p, String ghName) {
        if (isWorking(p)) {
            p.sendMessage(plugin.getConfigManager().getMessage("already-working"));
            return false;
        }

        int free = 0;
        for (ItemStack s : p.getInventory().getStorageContents()) {
            if (s == null || s.getType().isAir()) free++;
        }
        if (free < 4) {
            p.sendMessage(plugin.getConfigManager().getMessage("not-enough-space"));
            return false;
        }

        Greenhouse gh = plugin.getAreaManager().getGreenhouse(ghName);
        if (gh == null || !gh.isAreaSet()) {
            p.sendMessage(plugin.getConfigManager().getMessage("no-area-set"));
            return false;
        }

        sessions.put(p.getUniqueId(), new JobSession(p, ghName));
        p.getInventory().addItem(
                plugin.getItemManager().createWateringCan(),
                plugin.getItemManager().createFertilizer(),
                plugin.getItemManager().createPortableLamp(),
                plugin.getItemManager().createSprayer()
        );
        p.sendMessage(plugin.getConfigManager().getMessage("job-started"));
        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);

        BossBar bar = BossBar.bossBar(
                Component.text("\u2618 Ogrodnik ", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .append(Component.text("(" + ghName + ") | Wyleczono: 0 | \u2620 0 | \u2b50 0.00 zł", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, false)),
                0.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS
        );
        bossBars.put(p.getUniqueId(), bar);
        p.showBossBar(bar);

        return true;
    }

    public void updateBossBar(Player p) {
        JobSession session = sessions.get(p.getUniqueId());
        BossBar bar = bossBars.get(p.getUniqueId());
        if (session == null || bar == null) return;

        int healed = session.getHealedCount();
        int killed = session.getKilledCount();
        double reward = healed * plugin.getConfigManager().getRewardPerPlant();

        bar.name(Component.text("\u2618 Ogrodnik ", NamedTextColor.GREEN, TextDecoration.BOLD)
                .append(Component.text(String.format("| Wyleczono: %d | \u2620 %d | \u2b50 %.2f zł", healed, killed, reward), NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, false)));

        int max = plugin.getConfigManager().getMaxSickPlants();
        bar.progress(Math.min(1.0f, (float) healed / Math.max(1, max)));

        int total = healed + killed;
        if (total == 0) bar.color(BossBar.Color.GREEN);
        else {
            double ratio = (double) healed / total;
            bar.color(ratio >= 0.7 ? BossBar.Color.GREEN : (ratio >= 0.4 ? BossBar.Color.YELLOW : BossBar.Color.RED));
        }
    }

    public void endJob(Player p) {
        if (!isWorking(p)) {
            p.sendMessage(plugin.getConfigManager().getMessage("not-working"));
            return;
        }
        JobSession session = sessions.get(p.getUniqueId());
        restoreAllVisuals(p, session);
        removeJobItems(p);

        int healed = session.getHealedCount(), killed = session.getKilledCount();
        double reward = healed * plugin.getConfigManager().getRewardPerPlant();

        if (plugin.getEconomy() != null) plugin.getEconomy().depositPlayer(p, reward);

        long sec = session.getDurationSeconds();
        int total = healed + killed;
        int pct = total > 0 ? (int) Math.round(100.0 * healed / total) : 100;

        p.sendMessage("\u00a78\u00a7m═══════════════════════════════");
        p.sendMessage("  \u00a7a\u00a7l\u2618 Podsumowanie zmiany");
        p.sendMessage("  \u00a78\u00a7m─────────────────────────────");
        p.sendMessage(String.format("  \u00a77\u23f1 Czas pracy: \u00a7f%d min %ds", sec / 60, sec % 60));
        p.sendMessage("  \u00a77\ud83c\udf31 Wyleczonych: \u00a7a" + healed);
        p.sendMessage("  \u00a77\u2620 Zabitych: \u00a7c" + killed);
        p.sendMessage(String.format("  \u00a77\ud83d\udcb0 Zarobek: \u00a7e%.2f zł", reward));
        p.sendMessage("  \u00a77\u2b50 Skuteczność: " + colorPct(pct) + pct + "%");
        p.sendMessage("\u00a78\u00a7m═══════════════════════════════");

        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        removeBossBar(p);
        sessions.remove(p.getUniqueId());
    }

    private String colorPct(int pct) {
        return pct >= 80 ? "\u00a7a" : (pct >= 50 ? "\u00a7e" : "\u00a7c");
    }

    public void endJobSilently(Player p) {
        if (!isWorking(p)) return;
        restoreAllVisuals(p, sessions.get(p.getUniqueId()));
        removeJobItems(p);
        removeBossBar(p);
        sessions.remove(p.getUniqueId());
    }

    private void restoreAllVisuals(Player p, JobSession session) {
        if (!p.isOnline()) return;
        session.getSickPlantValues().forEach(data -> PacketHelper.restoreBlock(p, data.getLocation()));
        session.getExcludedValues().forEach(loc -> PacketHelper.restoreBlock(p, loc));
    }

    public void endAllSessions() {
        new ArrayList<>(sessions.keySet()).stream()
                .map(id -> plugin.getServer().getPlayer(id))
                .filter(Objects::nonNull)
                .forEach(this::endJobSilently);
        sessions.clear();
        bossBars.clear();
    }

    private void removeJobItems(Player p) {
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack item = p.getInventory().getItem(i);
            if (plugin.getItemManager().isJobItem(item)) p.getInventory().setItem(i, null);
        }
    }

    private void removeBossBar(Player p) {
        BossBar bar = bossBars.remove(p.getUniqueId());
        if (bar != null) p.hideBossBar(bar);
    }

    public boolean isWorking(Player p)        { return sessions.containsKey(p.getUniqueId()); }
    public JobSession getSession(Player p)     { return sessions.get(p.getUniqueId()); }
    public Collection<JobSession> getAllSessions() { return sessions.values(); }
}
