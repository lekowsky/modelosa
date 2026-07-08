package com.mondraq.job;

import org.bukkit.Location;
import org.bukkit.Material;

public final class SickPlantData {

    public enum State { SICK, BEING_INSPECTED, DIAGNOSED, DEAD }

    private final Location location;
    private final Material original;
    private Ailment ailment;
    private State state;

    public SickPlantData(Location location, Material original) {
        this.location = location;
        this.original = original;
        this.state = State.SICK;
    }

    public Location getLocation()    { return location; }
    public Material getOriginal()    { return original; }
    public Ailment getAilment()      { return ailment; }
    public void setAilment(Ailment a){ this.ailment = a; }
    public State getState()          { return state; }
    public void setState(State s)    { this.state = s; }
}
