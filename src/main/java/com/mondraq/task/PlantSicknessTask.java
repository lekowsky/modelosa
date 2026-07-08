package com.mondraq.task;

import com.mondraq.area.Greenhouse;
import com.mondraq.job.JobSession;
import com.mondraq.job.SickPlantData;
import com.mondraq.main.Main;
import com.mondraq.packet.PacketHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlantSicknessTask extends BukkitRunnable {

    private final Main plugin;

    public PlantSicknessTask(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int cycl = plugin.getConfigManager().getSickPlantsPerCycle();
        int max = plugin.getConfigManager().getMaxSickPlants();

        for (JobSession s : plugin.getJobManager().getAllSessions()) {
            Greenhouse gh = plugin.getAreaManager().getGreenhouse(s.getGreenhouseName());
            if (gh == null || !gh.isAreaSet()) continue;

            List<Location> flowers = gh.getFlowerBlocks();
            if (flowers.isEmpty()) continue;

            process(s, flowers, cycl, max);
        }
    }

    private void process(JobSession s, List<Location> flowers, int cycl, int max) {
        List<Location> active = new ArrayList<>();
        for (Location loc : flowers) {
            if (!s.isSickPlant(loc) && !s.isExcluded(loc)) active.add(loc);
        }

        int cur = s.getSickPlants().size();
        int canAdd = Math.max(0, max - cur);
        int toAdd = Math.min(cycl, Math.min(active.size(), canAdd));
        
        if (toAdd == 0) return;

        Collections.shuffle(active);
        boolean sound = false;
        
        for (int i = 0; i < toAdd; i++) {
            Location loc = active.get(i);
            Material orig = loc.getBlock().getType();
            s.addSickPlant(new SickPlantData(loc, orig));
            
            PacketHelper.sendFakeBlock(s.getPlayer(), loc, PacketHelper.SICK_BLOCK);

            if (!sound && s.getPlayer().isOnline()) {
                s.getPlayer().playSound(s.getPlayer().getLocation(), Sound.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA, 0.5f, 0.8f);
                sound = true;
            }
        }
    }
}
