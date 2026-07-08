package com.mondraq.area;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Greenhouse {

    private static final long CACHE_TTL = 60_000L;

    private final String name;
    private Location min, max;
    private World world;
    private UUID npcUUID;
    private List<Location> flowerCache = new ArrayList<>();
    private long cacheExpiry;

    public Greenhouse(String name) { this.name = name; }

    public Greenhouse(String name, Location min, Location max, World world) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.world = world;
    }

    public String getName()    { return name; }
    public Location getMin()   { return min; }
    public Location getMax()   { return max; }
    public World getWorld()    { return world; }
    public UUID getNpcUUID()   { return npcUUID; }

    public void setMin(Location min)    { this.min = min; }
    public void setMax(Location max)    { this.max = max; }
    public void setWorld(World world)   { this.world = world; }
    public void setNpcUUID(UUID uuid)   { this.npcUUID = uuid; }

    public boolean isAreaSet() {
        return min != null && max != null && world != null;
    }

    public boolean contains(Location loc) {
        if (!isAreaSet() || loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(world)) return false;
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        return x >= min.getX() && x <= max.getX() + 1
            && y >= min.getY() && y <= max.getY() + 1
            && z >= min.getZ() && z <= max.getZ() + 1;
    }

    public List<Location> getFlowerBlocks() {
        if (!isAreaSet()) return new ArrayList<>();
        long now = System.currentTimeMillis();
        if (now < cacheExpiry && !flowerCache.isEmpty())
            return new ArrayList<>(flowerCache);

        List<Location> result = new ArrayList<>();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
            for (int y = min.getBlockY(); y <= max.getBlockY() + 2; y++)
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Location loc = new Location(world, x, y, z);
                    if (AreaManager.FLOWER_MATERIALS.contains(loc.getBlock().getType()))
                        result.add(loc);
                }

        flowerCache = result;
        cacheExpiry = now + CACHE_TTL;
        return new ArrayList<>(result);
    }

    public void invalidateCache() {
        cacheExpiry = 0;
        flowerCache.clear();
    }
}
