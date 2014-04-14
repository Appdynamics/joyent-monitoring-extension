package com.appdynamics.monitors.joyent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

public abstract class StatsCollector {

    private static final Logger LOG = Logger.getLogger(StatsCollector.class);

    private static final String DATA_CENTER_URL = "https://api.joyentcloud.com/%s/datacenters";

    private Executor executor;

    public StatsCollector() {
        executor = new Executor();
    }

    public Executor executor() {
        return executor;
    }

    public abstract Map<String, ?> collectStats(String identity, String keyName, String privateKey);

    public Iterator<String> getAllDatacenters(String identity, String keyName, String privateKey) {
        String dataCenterURL = String.format(DATA_CENTER_URL, identity);
        Map<String, Number> instanceStatsMap = new LinkedHashMap<String, Number>();
        String dataCenterResponse = null;
        try {
            dataCenterResponse = executor().executeGetRequest(dataCenterURL, identity, keyName, privateKey);
        } catch (HttpException e) {
            LOG.error("Unable to execute request", e);
            throw new RuntimeException("Unable to execute request", e);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataCenterNode = null;
        try {
            dataCenterNode = objectMapper.readTree(dataCenterResponse);
        } catch (IOException e) {
            LOG.error("Unable parse data center response", e);
            throw new RuntimeException("Unable parse data center response", e);
        }
        return dataCenterNode.fieldNames();
    }

}
