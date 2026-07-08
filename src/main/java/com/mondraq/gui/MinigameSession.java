package com.mondraq.gui;

import org.bukkit.Location;

public final class MinigameSession {

    private final Location plantLoc;
    private int remainingSpots;

    public MinigameSession(Location plantLoc, int remainingSpots) {
        this.plantLoc = plantLoc;
        this.remainingSpots = remainingSpots;
    }

    public Location getPlantLoc() {
        return plantLoc;
    }

    public int getRemainingSpots() {
        return remainingSpots;
    }

    public void decrementSpots() {
        if (remainingSpots > 0) remainingSpots--;
    }
}
