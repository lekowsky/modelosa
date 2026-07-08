package com.mondraq.job;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class JobSession {

    private final Player player;
    private final String greenhouseName;
    private final Map<String, SickPlantData> sickPlants = new HashMap<>();
    private final Map<String, Location> excludedLocs = new HashMap<>();
    private final long startTime;
    
    private int healedCount = 0;
    private int killedCount = 0;
    private boolean inspecting = false;
    private String inspectLocKey = null;

    public JobSession(Player player, String greenhouseName) {
        this.player = player;
        this.greenhouseName = greenhouseName;
        this.startTime = System.currentTimeMillis();
    }

    public static String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public Player getPlayer()               { return player; }
    public String getGreenhouseName()       { return greenhouseName; }
    public Map<String, SickPlantData> getSickPlants() { return sickPlants; }
    public Collection<SickPlantData> getSickPlantValues() { return sickPlants.values(); }
    
    public int getHealedCount()             { return healedCount; }
    public void incrementHealed()           { healedCount++; }
    public int getKilledCount()             { return killedCount; }
    public void incrementKilled()           { killedCount++; }
    
    public boolean isInspecting()           { return inspecting; }
    public void setInspecting(boolean v)    { this.inspecting = v; }
    public String getInspectLocKey()        { return inspectLocKey; }
    public void setInspectLocKey(String k)  { this.inspectLocKey = k; }
    public long getStartTime()              { return startTime; }

    public long getDurationSeconds()        { return (System.currentTimeMillis() - startTime) / 1000; }

    public SickPlantData getSickPlant(Location loc) { return sickPlants.get(key(loc)); }
    public boolean isSickPlant(Location loc)        { return sickPlants.containsKey(key(loc)); }
    public void addSickPlant(SickPlantData data)    { sickPlants.put(key(data.getLocation()), data); }
    public void removeSickPlant(Location loc)       { sickPlants.remove(key(loc)); }

    public boolean isExcluded(Location loc)         { return excludedLocs.containsKey(key(loc)); }
    public void excludeLocation(Location loc)       { excludedLocs.put(key(loc), loc); }
    public void unexcludeLocation(Location loc)     { excludedLocs.remove(key(loc)); }
    public Collection<Location> getExcludedValues() { return excludedLocs.values(); }
}
