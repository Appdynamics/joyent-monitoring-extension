package com.appdynamics.monitors.joyent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

public class InstrumentationStats extends StatsCollector {

    private static final Logger LOG = Logger.getLogger(InstrumentationStats.class);

    private static final String CREATE_INSTRUMENTATION_URL = "https://%s.api.joyentcloud.com/%s/analytics/instrumentations";
    private static final String GET_INSTRUMENTATION_URL = "https://%s.api.joyentcloud.com/%s/analytics/instrumentations/%s/value/raw";
    private static final String DELETE_INSTRUMENTATION_URL = "https://%s.api.joyentcloud.com/%s/analytics/instrumentations/%s";

    private static final String METRIC_PATH = "Custom Metrics|Joyent|Instrumentation|%s|%s|%s|"; //Module Name|Stat Name|Zone

    private Instrumentation instrumentation;
    private Integer instrumentationsToRun;

    public InstrumentationStats(Instrumentation instrumentation, Integer maxInstrumentationsToRun) {
        this.instrumentation = instrumentation;
        this.instrumentationsToRun = maxInstrumentationsToRun;
    }

    @Override
    public Map<String, ?> collectStats(String identity, String keyName, String privateKey) {
        List<Module> modules = instrumentation.getModule();
        Map<String, String> instrumentationStats = createInstrumentationAndGetStats(modules, identity, keyName, privateKey);
        return instrumentationStats;
    }

    private Map<String, String> createInstrumentationAndGetStats(List<Module> modules, String identity, String keyName, String privateKey) {

        //Create instrumentation
        for (Module module : modules) {
            createInstrumentation(module, identity, keyName, privateKey);
        }

        Map<String, String> statsMap = new LinkedHashMap<String, String>();

        //Get instrumentation values
        for (Module module : modules) {
            getInstrumentationValue(module, identity, keyName, privateKey);
            Map<String, String> stringStringMap = buildStatsMap(module);
            statsMap.putAll(stringStringMap);
        }
        return statsMap;
    }

    private Map<String, String> buildStatsMap(Module module) {
        Map<String, String> statsMap = new LinkedHashMap<String, String>();
        String moduleName = module.getName();
        List<Stat> stats = module.getStat();
        for (Stat stat : stats) {
            if (stat.isEnabled()) {
                Map<String, String> instrumentationValues = stat.getInstrumentationValues();
                for (Map.Entry<String, String> entry : instrumentationValues.entrySet()) {
                    String statName = String.format(METRIC_PATH, moduleName, stat.getLabel(), entry.getKey());
                    statsMap.put(statName, entry.getValue());
                }
            }
        }
        return statsMap;
    }

    private void getInstrumentationValue(Module module, String identity, String keyName, String privateKey) {
        List<Stat> stats = module.getStat();
        ObjectMapper objectMapper = new ObjectMapper();
        for (Stat stat : stats) {
            if (stat.isEnabled()) {
                Map<String, Integer> instrumentationIds = stat.getInstrumentationIds();
                for (Map.Entry<String, Integer> instId : instrumentationIds.entrySet()) {
                    String instURL = String.format(GET_INSTRUMENTATION_URL, instId.getKey(), identity, instId.getValue());
                    try {
                        //Get the instrumentation value
                        String instrResp = executor().executeGetRequest(instURL, identity, keyName, privateKey);
                        JsonNode node = objectMapper.readTree(instrResp);
                        String instrValue = node.get("value").asText();
                        stat.setInstrumentationValue(instId.getKey(), instrValue);

                        //Delete the instrumentation
                        String deleteURL = String.format(DELETE_INSTRUMENTATION_URL, instId.getKey(), identity, instId.getValue());
                        executor().executeDeleteRequest(deleteURL, identity, keyName, privateKey);
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
        }

    }

    private void createInstrumentation(Module module, String identity, String keyName, String privateKey) {
        Iterator<String> allDatacenters = getAllDatacenters(identity, keyName, privateKey);
        ObjectMapper objectMapper = new ObjectMapper();
        while (allDatacenters.hasNext()) {
            String zone = allDatacenters.next();
            String createURL = String.format(CREATE_INSTRUMENTATION_URL, zone, identity);
            String moduleName = module.getName();
            List<Stat> stats = module.getStat();
            int noOfInstrumentations = 0;
            for (Stat stat : stats) {
                if (stat.isEnabled()) {
                    if (noOfInstrumentations > instrumentationsToRun) {
                        LOG.info("Executed maximum number of instrumentations configured. Ignoring remaining instrumentations if any");
                        break;
                    }
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("module", moduleName);
                    params.put("stat", stat.getName());
                    try {
                        String createInstrumentationResponse = executor().executePostRequest(createURL, params, identity, keyName, privateKey);
                        JsonNode node = objectMapper.readTree(createInstrumentationResponse);
                        int instrumentationId = node.get("id").asInt();
                        stat.setInstrumentationId(zone, instrumentationId);
                        noOfInstrumentations++;
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
        }
    }
}
