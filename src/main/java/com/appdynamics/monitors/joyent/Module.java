package com.appdynamics.monitors.joyent;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Module {

    private String name;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Stat> stat;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addStat(Stat statToAdd) {
        if (stat == null) {
            stat = new ArrayList<Stat>();
        }
        stat.add(statToAdd);
    }

    public void addStats(List<Stat> stats) {
        if (stat == null) {
            stat = new ArrayList<Stat>();
        }
        stat.addAll(stats);
    }

    public List<Stat> getStat() {
        return stat;
    }

    public void setStat() {
        this.stat = stat;
    }
}

class Stat {
    private String name;
    private String label;
    private boolean enabled;
    private Map<String, Integer> instrumentationIdPerZone;
    private Map<String, String> instrumentationValuePerZone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setInstrumentationId(String zone, Integer instrumentationId) {
        if (instrumentationIdPerZone == null) {
            instrumentationIdPerZone = new HashMap<String, Integer>();
        }
        instrumentationIdPerZone.put(zone, instrumentationId);
    }

    public Map<String, Integer> getInstrumentationIds() {
        if (instrumentationIdPerZone == null) {
            instrumentationIdPerZone = new HashMap<String, Integer>();
        }
        return instrumentationIdPerZone;
    }

    public void setInstrumentationValue(String zone, String instrumentationValue) {
        if (instrumentationValuePerZone == null) {
            instrumentationValuePerZone = new HashMap<String, String>();
        }
        instrumentationValuePerZone.put(zone, instrumentationValue);
    }

    public Map<String, String> getInstrumentationValues() {
        if (instrumentationValuePerZone == null) {
            instrumentationValuePerZone = new HashMap<String, String>();
        }
        return instrumentationValuePerZone;
    }
}