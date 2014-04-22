package com.appdynamics.monitors.joyent;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XStreamAlias("module")
public class Module {


    @XStreamAlias("name")
    @XStreamAsAttribute
    private String name;

    @XStreamImplicit(itemFieldName="stat")
    private List<Stat> stats;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addStat(Stat statToAdd) {
        if (stats == null) {
            stats = new ArrayList<Stat>();
        }
        stats.add(statToAdd);
    }

    public void addStats(List<Stat> stats) {
        if (stats == null) {
            stats = new ArrayList<Stat>();
        }
        stats.addAll(stats);
    }

    public List<Stat> getStat() {
        return stats;
    }

    public void setStat(List<Stat> stats) {
        this.stats = stats;
    }
}

@XStreamAlias("stat")
class Stat {

    @XStreamAlias("name")
    private String name;
    @XStreamAlias("label")
    private String label;
    @XStreamAlias("enabled")
    @XStreamAsAttribute
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