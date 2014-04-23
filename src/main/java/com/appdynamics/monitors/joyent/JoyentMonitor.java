package com.appdynamics.monitors.joyent;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.util.Map;
import org.apache.log4j.Logger;

public class JoyentMonitor extends AManagedMonitor {
    private static final Logger LOG = Logger.getLogger(JoyentMonitor.class);

    public JoyentMonitor() {
        String details = JoyentMonitor.class.getPackage().getImplementationTitle();
        String msg = "Using Monitor Version [" + details + "]";
        LOG.info(msg);
        System.out.println(msg);
    }

    public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {

        String identity = taskArguments.get("identity");
        String privateKey = taskArguments.get("joyent-private-key");
        String keyName = taskArguments.get("joyent-key-name");

        if (identity == null || privateKey == null || keyName == null ||
                identity.length() <= 0 || privateKey.length() <= 0 || keyName.length() <= 0) {
            LOG.error("Invalid or no arguments provided. Please provide required arguments in monitor.xml");
            throw new TaskExecutionException("Invalid or no arguments provided. Please provide required arguments in monitor.xml");
        }


        MachineStatsCollector machineStatsCollector = new MachineStatsCollector();
        Map<String, ?> machineStats = machineStatsCollector.collectStats(identity, keyName, privateKey);
        printMetric(machineStats);

        String instrumentXMLPath = taskArguments.get("instrumentation-file-path");
        Instrumentation instrumentation = null;
        if (instrumentXMLPath != null) {
            instrumentation = parseAndPopulate(instrumentXMLPath);
        }

        if (instrumentation != null && instrumentation.getModules() != null && instrumentation.getModules().size() != 0) {
            String maxInstrumentationsToRun = taskArguments.get("max-instrumentations-to-run");
            Integer instrumentationsToRun = 0;
            try {
                instrumentationsToRun = Integer.valueOf(maxInstrumentationsToRun);
            } catch (NumberFormatException e) {
                LOG.error("Invalid number provided for max-instrumentations-to-run", e);
            }
            if (instrumentationsToRun > 0) {
                InstrumentationStats instrumentationStats = new InstrumentationStats(instrumentation, instrumentationsToRun);
                Map<String, ?> instrumentationStatsMap = instrumentationStats.collectStats(identity, keyName, privateKey);
                printMetric(instrumentationStatsMap);
            }
        } else {
            LOG.info("No instrumentations defined to run");
        }


        return new TaskOutput("JoyentMonitor completed successfully");
    }

    private Instrumentation parseAndPopulate(String instrumentXMLPath) {

        XStream xstream = new XStream();
        xstream.alias("instrumentations", Instrumentation.class);
        xstream.alias("module", Module.class);
        xstream.alias("stat", Stat.class);
        xstream.autodetectAnnotations(true);
        Instrumentation instrumentation = (Instrumentation) xstream.fromXML(new File(instrumentXMLPath));
        return instrumentation;
    }

    private void printMetric(Map<String, ?> metrics) {

        for (Map.Entry<String, ?> metric : metrics.entrySet()) {
            MetricWriter metricWriter = super.getMetricWriter(metric.getKey(), MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE, MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE, MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
            );
            Object metricValue = metric.getValue();
            if (metricValue instanceof Double) {
                metricWriter.printMetric(String.valueOf(Math.round((Double) metricValue)));
            } else if (metricValue instanceof Float) {
                metricWriter.printMetric(String.valueOf(Math.round((Float) metricValue)));
            } else {
                metricWriter.printMetric(String.valueOf(metricValue));
            }
        }
    }

    public static void main(String[] args) {
        JoyentMonitor joyentMonitor = new JoyentMonitor();
        Instrumentation instrumentation = joyentMonitor.parseAndPopulate("/home/satish/AppDynamics/Code/extensions/joyent-monitoring-extension/src/main/resources/config/instrumentations1.xml");
        System.out.println(instrumentation);
    }
}
