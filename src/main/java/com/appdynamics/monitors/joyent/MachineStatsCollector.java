package com.appdynamics.monitors.joyent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.log4j.Logger;

public class MachineStatsCollector extends StatsCollector {

    private static final Logger LOG = Logger.getLogger(MachineStatsCollector.class);

    private static final String INSTANCE_URL = "https://%s.api.joyentcloud.com/%s/machines";

    private static final String METRIC_PATH = "Custom Metrics|Joyent|Instances|%s|%s|";

    @Override
    public Map<String, ?> collectStats(String identity, String keyName, String privateKey) {
        LOG.info("Fetching machine stats");
        Map<String, Number> instanceStatsMap = new LinkedHashMap<String, Number>();
        try {
            Iterator<String> allDatacenters = getAllDatacenters(identity, keyName, privateKey);
            while (allDatacenters.hasNext()) {
                String zone = allDatacenters.next();
                String instanceURL = String.format(INSTANCE_URL, zone, identity);
                String instanceResponse = executor().executeGetRequest(instanceURL, identity, keyName, privateKey);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode instanceNodes = objectMapper.readTree(instanceResponse);

                Iterator<JsonNode> instanceNodeElements = instanceNodes.elements();
                while (instanceNodeElements.hasNext()) {
                    JsonNode instanceNode = instanceNodeElements.next();
                    String name = instanceNode.get("name").asText();
                    String metricName = String.format(METRIC_PATH, zone, name);
                    instanceStatsMap.put(metricName + "Memory", instanceNode.get("memory").asInt());
                    instanceStatsMap.put(metricName + "Disk", instanceNode.get("disk").asInt());
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            //Ignore and continue
        }
        return instanceStatsMap;
    }
}
