package com.mondraq.area;

import com.mondraq.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public final class AreaManager {

    public static final Set<Material> FLOWER_MATERIALS = EnumSet.of(
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID,
            Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP,
            Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY,
            Material.TORCHFLOWER, Material.PINK_PETALS, Material.SUNFLOWER,
            Material.LILAC, Material.ROSE_BUSH, Material.PEONY
    );

    private final Main plugin;
    private final Map<String, Greenhouse> greenhouses = new LinkedHashMap<>();
    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();
    private final Map<UUID, String> pending = new HashMap<>();

    public AreaManager(Main plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        FileConfiguration cfg = plugin.getConfig();

        if (cfg.contains("area.world") && !cfg.contains("greenhouses"))
            migrate(cfg);

        ConfigurationSection sec = cfg.getConfigurationSection("greenhouses");
        if (sec == null) return;

        for (String name : sec.getKeys(false)) {
            Greenhouse gh = new Greenhouse(name);
            String base = "greenhouses." + name;
            String wn = cfg.getString(base + ".area.world", "");
            if (!wn.isEmpty()) {
                World w = Bukkit.getWorld(wn);
                if (w != null) {
                    gh.setWorld(w);
                    gh.setMin(new Location(w, cfg.getDouble(base + ".area.min.x"),
                            cfg.getDouble(base + ".area.min.y"), cfg.getDouble(base + ".area.min.z")));
                    gh.setMax(new Location(w, cfg.getDouble(base + ".area.max.x"),
                            cfg.getDouble(base + ".area.max.y"), cfg.getDouble(base + ".area.max.z")));
                }
            }
            String npc = cfg.getString(base + ".npc.uuid", "");
            if (!npc.isEmpty()) {
                try { gh.setNpcUUID(UUID.fromString(npc)); } catch (IllegalArgumentException ignored) {}
            }
            greenhouses.put(name, gh);
        }
    }

    private void migrate(FileConfiguration cfg) {
        plugin.getLogger().info("Migracja starego formatu konfiguracji...");
        String wn = cfg.getString("area.world", "");
        cfg.set("greenhouses.default.area.world", wn);
        for (String axis : new String[]{"x", "y", "z"}) {
            cfg.set("greenhouses.default.area.min." + axis, cfg.getDouble("area.min." + axis));
            cfg.set("greenhouses.default.area.max." + axis, cfg.getDouble("area.max." + axis));
        }
        String npc = cfg.getString("npc.uuid", "");
        if (!npc.isEmpty()) cfg.set("greenhouses.default.npc.uuid", npc);
        cfg.set("area", null);
        cfg.set("npc", null);
        plugin.saveConfig();
    }

    public void reload() {
        greenhouses.clear();
        load();
    }

    public Greenhouse getGreenhouse(String name)       { return greenhouses.get(name); }
    public Collection<Greenhouse> getAllGreenhouses()   { return greenhouses.values(); }
    public Set<String> getGreenhouseNames()            { return greenhouses.keySet(); }

    public Greenhouse getOrCreate(String name) {
        return greenhouses.computeIfAbsent(name, Greenhouse::new);
    }

    public void removeGreenhouse(String name) {
        Greenhouse gh = greenhouses.remove(name);
        if (gh == null) return;
        if (gh.getNpcUUID() != null) {
            for (World w : plugin.getServer().getWorlds()) {
                var e = w.getEntity(gh.getNpcUUID());
                if (e != null) { e.remove(); break; }
            }
        }
        plugin.getConfig().set("greenhouses." + name, null);
        plugin.saveConfig();
    }

    public Greenhouse getGreenhouseByNpc(UUID npcId) {
        if (npcId == null) return null;
        for (Greenhouse gh : greenhouses.values())
            if (npcId.equals(gh.getNpcUUID())) return gh;
        return null;
    }

    public Greenhouse getGreenhouseAt(Location loc) {
        for (Greenhouse gh : greenhouses.values())
            if (gh.contains(loc)) return gh;
        return null;
    }

    public boolean containsAny(Location loc) { return getGreenhouseAt(loc) != null; }
    public boolean isSet() {
        for (Greenhouse gh : greenhouses.values())
            if (gh.isAreaSet()) return true;
        return false;
    }

    public List<Location> getFlowerBlocks(String name) {
        Greenhouse gh = greenhouses.get(name);
        return gh != null ? gh.getFlowerBlocks() : new ArrayList<>();
    }

    public void invalidateCache(Location loc) {
        Greenhouse gh = getGreenhouseAt(loc);
        if (gh != null) gh.invalidateCache();
    }

    public void invalidateAllCaches() {
        greenhouses.values().forEach(Greenhouse::invalidateCache);
    }



    public void setPos1(UUID id, Location loc) { pos1.put(id, loc.getBlock().getLocation()); }
    public void setPos2(UUID id, Location loc) { pos2.put(id, loc.getBlock().getLocation()); }
    public Location getPos1(UUID id)           { return pos1.get(id); }
    public Location getPos2(UUID id)           { return pos2.get(id); }
    public void startSettingUp(UUID id, String name) { pending.put(id, name); }
    public String getSettingUp(UUID id) { return pending.get(id); }

    public void clearSelections(UUID id) {
        pos1.remove(id);
        pos2.remove(id);
        pending.remove(id);
    }

    public boolean confirmArea(UUID id) {
        String name = pending.get(id);
        return name != null && confirmArea(id, name);
    }

    public boolean confirmArea(UUID id, String name) {
        Location p1 = pos1.get(id), p2 = pos2.get(id);
        if (p1 == null || p2 == null) return false;
        if (p1.getWorld() == null || p2.getWorld() == null || !p1.getWorld().equals(p2.getWorld())) return false;

        World w = p1.getWorld();
        Location mn = new Location(w, Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
        Location mx = new Location(w, Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ()));

        Greenhouse gh = getOrCreate(name);
        gh.setWorld(w);
        gh.setMin(mn);
        gh.setMax(mx);
        gh.invalidateCache();

        pos1.remove(id);
        pos2.remove(id);
        pending.remove(id);
        save(gh);
        return true;
    }

    public void save(Greenhouse gh) {
        String p = "greenhouses." + gh.getName();
        FileConfiguration cfg = plugin.getConfig();
        if (gh.isAreaSet()) {
            cfg.set(p + ".area.world", gh.getWorld().getName());
            cfg.set(p + ".area.min.x", gh.getMin().getBlockX());
            cfg.set(p + ".area.min.y", gh.getMin().getBlockY());
            cfg.set(p + ".area.min.z", gh.getMin().getBlockZ());
            cfg.set(p + ".area.max.x", gh.getMax().getBlockX());
            cfg.set(p + ".area.max.y", gh.getMax().getBlockY());
            cfg.set(p + ".area.max.z", gh.getMax().getBlockZ());
        }
        if (gh.getNpcUUID() != null) cfg.set(p + ".npc.uuid", gh.getNpcUUID().toString());
        plugin.saveConfig();
    }
}
