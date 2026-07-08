package com.mondraq.job;

public enum Ailment {
    DRYING("ailment-drying", "watering_can"),
    LACKS_VITAMINS("ailment-vitamins", "fertilizer"),
    NEEDS_LIGHT("ailment-light", "portable_lamp"),
    INFESTED("ailment-infested", "sprayer");

    private final String msgKey;
    private final String toolId;

    Ailment(String msgKey, String toolId) {
        this.msgKey = msgKey;
        this.toolId = toolId;
    }

    public String getMessageKey() { return msgKey; }
    public String getToolId()     { return toolId; }

    public static Ailment random() {
        Ailment[] vals = values();
        return vals[(int) (Math.random() * vals.length)];
    }
}
