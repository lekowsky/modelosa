package com.mondraq.task;

import com.mondraq.job.Ailment;
import com.mondraq.job.JobSession;
import com.mondraq.job.SickPlantData;
import com.mondraq.main.Main;
import com.mondraq.packet.PacketHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class InspectionTask extends BukkitRunnable {

    private final Main plugin;
    private final Player p;
    private final Location plantLoc;
    private final int totalSec;
    private int reqSec = 0;

    public InspectionTask(Main plugin, Player p, Location plantLoc) {
        this.plugin = plugin;
        this.p = p;
        this.plantLoc = plantLoc;
        this.totalSec = plugin.getConfigManager().getInspectionDurationSeconds();
    }

    @Override
    public void run() {
        if (!p.isOnline() || !plugin.getJobManager().isWorking(p)) {
            cancel();
            cleanup();
            return;
        }

        reqSec++;
        int rem = totalSec - reqSec;

        StringBuilder bar = new StringBuilder("\u00a7e\u00a7l[\u00a7a");
        int filled = (int) Math.ceil(((double) reqSec / totalSec) * 10);
        for (int i = 0; i < 10; i++) bar.append(i < filled ? "\u25a0" : "\u00a7c\u25a0");
        bar.append("\u00a7e\u00a7l] \u00a7fInspekcja... \u00a7e").append(rem).append("s");
        p.sendActionBar(Component.text(bar.toString()));

        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 1.8f);

        if (reqSec >= totalSec) {
            cancel();
            finish();
        }
    }

    private void finish() {
        if (!p.isOnline()) return;
        JobSession session = plugin.getJobManager().getSession(p);
        if (session == null) return;

        SickPlantData data = session.getSickPlant(plantLoc);
        if (data == null) {
            cleanup();
            return;
        }

        Ailment a = Ailment.random();
        data.setAilment(a);
        data.setState(SickPlantData.State.DIAGNOSED);
        
        session.setInspecting(false);
        session.setInspectLocKey(null);

        p.sendActionBar(Component.text(""));
        p.sendMessage(plugin.getConfigManager().getMessage(a.getMessageKey()));
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.4f);

        PacketHelper.resendSickBlock(p, plantLoc);
    }

    private void cleanup() {
        JobSession session = plugin.getJobManager().getSession(p);
        if (session != null) {
            SickPlantData data = session.getSickPlant(plantLoc);
            if (data != null && data.getState() == SickPlantData.State.BEING_INSPECTED) {
                data.setState(SickPlantData.State.SICK);
            }
            session.setInspecting(false);
            session.setInspectLocKey(null);
        }
    }
}
