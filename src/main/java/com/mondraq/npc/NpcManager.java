package com.mondraq.npc;

import com.mondraq.area.Greenhouse;
import com.mondraq.main.Main;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public final class NpcManager {

    private final Main plugin;

    public NpcManager(Main plugin) {
        this.plugin = plugin;
    }

    public void spawnNpc(Location loc, String ghName) {
        Greenhouse gh = plugin.getAreaManager().getOrCreate(ghName);

        if (gh.getNpcUUID() != null) {
            NPC existing = CitizensAPI.getNPCRegistry().getByUniqueId(gh.getNpcUUID());
            if (existing != null) {
                existing.destroy();
            }
        }

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "\u00a76\u00a7l\u25b6 Ogrodnik \u00a7e(" + ghName + ")");
        
        String skinName = plugin.getConfig().getString("job.npc-skin", "Farmer");
        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinName(skinName);

        npc.spawn(loc);

        gh.setNpcUUID(npc.getUniqueId());
        plugin.getAreaManager().save(gh);
    }

    public boolean isNpc(NPC npc) {
        return getGreenhouseName(npc) != null;
    }

    public String getGreenhouseName(NPC npc) {
        if (npc == null) return null;
        UUID npcId = npc.getUniqueId();
        Greenhouse gh = plugin.getAreaManager().getGreenhouseByNpc(npcId);
        return gh != null ? gh.getName() : null;
    }
}
