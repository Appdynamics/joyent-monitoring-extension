/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

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
                    instanceStatsMap.put(metricName + "State", State.getStateInt(instanceNode.get("state").asText()));
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            //Ignore and continue
        }
        return instanceStatsMap;
    }

    private enum State {
        Provisioning("Provisioning"), Failed("failed"), Running("Running"), Stopping("Stopping"), Stopped("Stopped"), Deleted("Deleted"), Offline("offline"), UnDefined("Undefined");
        private String state;

        State(String state) {
            this.state = state;
        }
        
        public String getState() {
            return state;
        }
        
        public static int getStateInt(String state) {
            State[] statesEnum = State.values();
            for(State stateEnum : statesEnum) {
                if(stateEnum.getState().equalsIgnoreCase(state)) {
                    return stateEnum.ordinal();
                }
            }
            return UnDefined.ordinal();
        }
    }
}
