/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.joyent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

public class InstrumentationStats extends StatsCollector {

    private static final Logger LOG = Logger.getLogger(InstrumentationStats.class);

    private static final String LIST_INSTRUMENTATION_URL = "https://%s.api.joyentcloud.com/%s/analytics/instrumentations";
    private static final String GET_INSTRUMENTATION_URL = "https://%s.api.joyentcloud.com/%s/analytics/instrumentations/%s/value/raw";
    private static final String METRIC_PATH = "Custom Metrics|Joyent|Instrumentation|%s|%s|%s"; //Module Name|Stat Name|Zone

    @Override
    public Map<String, ?> collectStats(String identity, String keyName, String privateKey) {
        LOG.info("Fetching instrumentation stats");
        Map<String, String> instrumentationStats = getInstrumentationsWithStats(identity, keyName, privateKey);
        return instrumentationStats;
    }

    private Map<String, String> getInstrumentationsWithStats(String identity, String keyName, String privateKey) {

        List<Instrumentation> instrumentations = listInstrumentations(identity, keyName, privateKey);

        populateInstrumentationValue(instrumentations, identity, keyName, privateKey);

        Map<String, String> statsMap = buildStatsMap(instrumentations);

        return statsMap;
    }

    /**
     * Fetches instrumentations from Joyent
     *
     * @param identity
     * @param keyName
     * @param privateKey
     * @return list of instrumentations
     */
    private List<Instrumentation> listInstrumentations(String identity, String keyName, String privateKey) {

        Iterator<String> allDatacentersItr = getAllDatacenters(identity, keyName, privateKey);
        ArrayList<String> allDatacenters = Lists.newArrayList(allDatacentersItr);

        List<Instrumentation> instrumentations = new ArrayList<Instrumentation>();

        ObjectMapper objectMapper = new ObjectMapper();
        for (String zone : allDatacenters) {
            String listInstrumentationsURL = String.format(LIST_INSTRUMENTATION_URL, zone, identity);
            try {
                String instrResp = executor().executeGetRequest(listInstrumentationsURL, identity, keyName, privateKey);
                JsonNode node = objectMapper.readTree(instrResp);
                Iterator<JsonNode> elements = node.elements();
                while (elements.hasNext()) {
                    JsonNode instrumentationNode = elements.next();

                    Instrumentation instrumentation = new Instrumentation();
                    instrumentation.setZone(zone);
                    instrumentation.setId(instrumentationNode.get("id").asText());
                    instrumentation.setModule(instrumentationNode.get("module").asText());
                    instrumentation.setStat(instrumentationNode.get("stat").asText());
                    JsonNode zoneNode = instrumentationNode.get("predicate").get("eq");
                    if (zoneNode != null) {
                        Iterator<JsonNode> zoneElement = zoneNode.elements();
                        zoneElement.next();
                        instrumentation.setUuid(zoneElement.next().asText());
                    }
                    instrumentations.add(instrumentation);
                }
            } catch (HttpException e) {
                LOG.error("Unable to execute request", e);
                throw new RuntimeException("Unable to execute request", e);
            } catch (JsonProcessingException e) {
                LOG.error("Unable to parse response", e);
                throw new RuntimeException("Unable to parse response", e);
            } catch (IOException e) {
                LOG.error("Unable to parse response", e);
                throw new RuntimeException("Unable to parse response", e);
            }
        }
        return instrumentations;
    }

    /**
     * Builds stats map from the instrumentation
     *
     * @param instrumentations
     * @return stats map
     */
    private Map<String, String> buildStatsMap(List<Instrumentation> instrumentations) {
        Map<String, String> statsMap = new LinkedHashMap<String, String>();
        for (Instrumentation instrumentation : instrumentations) {
            String statName = String.format(METRIC_PATH, instrumentation.getModule(), instrumentation.getStat(), instrumentation.getZone());
            if (instrumentation.getUuid() != null) {
                statName += "|" + instrumentation.getUuid();
            }
            statsMap.put(statName, instrumentation.getValue());
        }
        return statsMap;
    }

    /**
     * For every instrumentation fetches its value from Joyent
     *
     * @param instrumentations
     * @param identity
     * @param keyName
     * @param privateKey
     */
    private void populateInstrumentationValue(List<Instrumentation> instrumentations, String identity, String keyName, String privateKey) {
        ObjectMapper objectMapper = new ObjectMapper();
        for (Instrumentation instrumentation : instrumentations) {

            String instURL = String.format(GET_INSTRUMENTATION_URL, instrumentation.getZone(), identity, instrumentation.getId());
            try {
                //Get the instrumentation value
                String instrResp = executor().executeGetRequest(instURL, identity, keyName, privateKey);
                JsonNode node = objectMapper.readTree(instrResp);
                JsonNode valueNode = node.get("value");
                String instrValue = null;
                if (valueNode.isValueNode()) {
                    instrValue = getStringValue(valueNode);
                } else {
                    Iterator<JsonNode> elements = valueNode.elements();
                    if (elements != null) {
                        JsonNode jsonNode = elements.next();
                        instrValue = getStringValue(jsonNode);
                    }
                }
                instrumentation.setValue(instrValue);
            } catch (HttpException e) {
                LOG.error("Unable to execute request", e);
                throw new RuntimeException("Unable to execute request", e);
            } catch (JsonProcessingException e) {
                LOG.error("Unable to parse response", e);
                throw new RuntimeException("Unable to parse response", e);
            } catch (IOException e) {
                LOG.error("Unable to parse response", e);
                throw new RuntimeException("Unable to parse response", e);
            }
        }
    }

    private String getStringValue(JsonNode valueNode) {
        String instrValue;
        if (valueNode.isDouble()) {
            instrValue = String.valueOf(Math.round(valueNode.asDouble()));
        } else {
            instrValue = valueNode.asText();
        }
        return instrValue;
    }

    public static void main(String[] args) {
        InstrumentationStats instrumentationStats = new InstrumentationStats();
        Map<String, ?> stringMap = instrumentationStats.collectStats("rgiroti@appdynamics.com", "appd-joyent", "/home/satish/AppDynamics/Joyent/appd-joyent_id_rsa");
        System.out.println(stringMap);
    }
}
