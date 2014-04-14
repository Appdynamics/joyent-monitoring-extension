package com.appdynamics.monitors.joyent;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.List;

public class Instrumentation {

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Module> module;

    public List<Module> getModule() {
        return module;
    }

    public void setModule(List<Module> module) {
        this.module = module;
    }
}
