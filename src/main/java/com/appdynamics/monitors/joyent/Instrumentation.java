package com.appdynamics.monitors.joyent;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.List;

@XStreamAlias("instrumentations")
public class Instrumentation {

    @XStreamImplicit(itemFieldName="module")
    private List<Module> module;

    public List<Module> getModule() {
        return module;
    }

    public void setModule(List<Module> module) {
        this.module = module;
    }
}
