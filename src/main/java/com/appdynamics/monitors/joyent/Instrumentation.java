package com.appdynamics.monitors.joyent;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.List;

@XStreamAlias("instrumentations")
public class Instrumentation {

    @XStreamImplicit(itemFieldName="module")
    private List<Module> modules;

    public List<Module> getModules() {
        return modules;
    }

    public void setModule(List<Module> modules) {
        this.modules = modules;
    }
}
