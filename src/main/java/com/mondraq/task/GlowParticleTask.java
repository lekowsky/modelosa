package com.mondraq.task;

import com.mondraq.job.JobSession;
import com.mondraq.job.SickPlantData;
import com.mondraq.main.Main;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class GlowParticleTask extends BukkitRunnable {

    private static final double MAX_DIST_SQ = 32.0 * 32.0;
    private static final Particle.DustOptions SICK_COLOR       = new Particle.DustOptions(Color.YELLOW, 1.0f);
    private static final Particle.DustOptions DIAGNOSED_COLOR  = new Particle.DustOptions(Color.RED, 1.2f);
    private static final Particle.DustOptions INSPECTING_COLOR = new Particle.DustOptions(Color.AQUA, 1.0f);
    private static final Particle.DustOptions DEAD_COLOR       = new Particle.DustOptions(Color.fromRGB(64, 64, 64), 0.8f);

    private final Main plugin;

    public GlowParticleTask(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (JobSession session : plugin.getJobManager().getAllSessions()) {
            Player p = session.getPlayer();
            if (!p.isOnline()) continue;
            Location pLoc = p.getLocation();

            for (SickPlantData data : session.getSickPlantValues()) {
                Location plantLoc = data.getLocation();
                if (plantLoc.getWorld() == null || !plantLoc.getWorld().equals(pLoc.getWorld())) continue;
                if (plantLoc.distanceSquared(pLoc) > MAX_DIST_SQ) continue;

                Location center = plantLoc.clone().add(0.5, 0.5, 0.5);
                p.spawnParticle(Particle.DUST, center, 10, 0.3, 0.3, 0.3, 0, getColor(data.getState()));
            }
        }
    }

    private Particle.DustOptions getColor(SickPlantData.State state) {
        return switch (state) {
            case SICK             -> SICK_COLOR;
            case BEING_INSPECTED  -> INSPECTING_COLOR;
            case DIAGNOSED        -> DIAGNOSED_COLOR;
            case DEAD             -> DEAD_COLOR;
        };
    }
}
