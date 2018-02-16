/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.joyent;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
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
        LOG.info("Executing Joyent Monitoring extension");
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

        InstrumentationStats instrumentationStats = new InstrumentationStats();
        Map<String, ?> instrumentationStatsMap = instrumentationStats.collectStats(identity, keyName, privateKey);
        printMetric(instrumentationStatsMap);

        return new TaskOutput("Joyent Monitor completed successfully");
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

    public static void main(String[] args) throws TaskExecutionException {
        JoyentMonitor joyentMonitor = new JoyentMonitor();
        joyentMonitor.execute(null, null);
    }
}
