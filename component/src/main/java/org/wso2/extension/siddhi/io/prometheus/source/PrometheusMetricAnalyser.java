/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.io.prometheus.source;

import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class analyses the response from http response, filter the metrics and generate maps according to
 * metric label-values.
 */
class PrometheusMetricAnalyser {

    private static final Logger log = Logger.getLogger(PrometheusMetricAnalyser.class);
    private final String metricName;
    private final MetricType metricType;
    private final SourceEventListener sourceEventListener;
    private final List<String> lastValidSample = new ArrayList<>();
    private final Attribute.Type valueType;
    private final String metricJob;
    private final String metricInstance;
    private final Map<String, String> metricGroupingKey;
    private String metricHelp;


    PrometheusMetricAnalyser(String metricName, MetricType metricType, String metricJob, String metricInstance,
                             Map<String, String> metricGroupingKey, Attribute.Type valueType,
                             SourceEventListener sourceEventListener) {
        this.metricName = metricName;
        this.metricType = metricType;
        this.metricJob = metricJob;
        this.metricInstance = metricInstance;
        this.metricGroupingKey = metricGroupingKey;
        this.valueType = valueType;
        this.sourceEventListener = sourceEventListener;
    }

    void analyseMetrics(List<String> metricSamples, String targetURL, String streamID) {
        int index = -1;
        for (int i = 0; i < metricSamples.size(); i++) {
            if ((metricSamples.get(i)).startsWith("# TYPE " + metricName + " ")) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            String error = "The specified metric cannot be found inside the http response from the " + targetURL +
                    " of " + PrometheusConstants.PROMETHEUS_SOURCE + " associated with stream \'" + streamID +
                    "\'.";
            log.error(error, new SiddhiAppRuntimeException(error));
        }
        assignHelpString(metricSamples, index);
        if (!checkMetricType(metricSamples, index)) {
            String error = " The type of the metric retrieved from the target \'" + targetURL + "\' is not " +
                    "matching with the specified metric type \'" + MetricType.getMetricTypeString(metricType) +
                    "\' in the " + PrometheusConstants.PROMETHEUS_SOURCE + " associated with stream \'" +
                    streamID + "\'. ";
            log.error(error, new SiddhiAppRuntimeException(error));
        }
        List<String> retrievedMetrics = metricSamples.stream().filter(
                response -> response.startsWith(metricName)).collect(Collectors.toList());
        List<String> filteredMetrics = new ArrayList<>(retrievedMetrics);
        if ((!metricJob.equals(PrometheusConstants.EMPTY_STRING) ||
                !metricInstance.equals(PrometheusConstants.EMPTY_STRING) || !metricGroupingKey.isEmpty())) {
            for (String sampleSingleLine : retrievedMetrics) {
                Map<String, String> labelPairMap = filterMetric(sampleSingleLine);
                if (!(metricJob.equals(PrometheusConstants.EMPTY_STRING))) {
                    String job = labelPairMap.get("job");
                    if (job == null || !job.equalsIgnoreCase(metricJob)) {
                        filteredMetrics.remove(sampleSingleLine);
                        continue;
                    }
                }
                if (!(metricInstance.equals(PrometheusConstants.EMPTY_STRING))) {
                    String instance = labelPairMap.get("instance");
                    if (instance == null || !instance.equalsIgnoreCase(metricInstance)) {
                        filteredMetrics.remove(sampleSingleLine);
                        continue;
                    }
                }
                if (metricGroupingKey != null) {
                    for (Map.Entry<String, String> entry : metricGroupingKey.entrySet()) {
                        String value = labelPairMap.get(entry.getKey());
                        if (value != null) {
                            if (!value.equalsIgnoreCase(entry.getValue())) {
                                filteredMetrics.remove(sampleSingleLine);
                                break;
                            }
                        } else {
                            //if the grouping key not found in the metric,
                            filteredMetrics.remove(sampleSingleLine);
                            break;
                        }
                    }
                }
            }
            if (filteredMetrics.isEmpty()) {
                String error = " The job, instance or grouping key of the metric retrieved from the target at" +
                        " \'" + targetURL + "\' is not matching with the specified metric in the " +
                        PrometheusConstants.PROMETHEUS_SOURCE + " associated with stream \'" + streamID +
                        "\'. ";
                log.error(error, new SiddhiAppRuntimeException(error));
            }
        }
        lastValidSample.clear();
        lastValidSample.addAll(filteredMetrics);
        generateMaps(filteredMetrics);
    }

    private void assignHelpString(List<String> metricSamples, int index) {
        if (index == 0) {
            this.metricHelp = PrometheusConstants.EMPTY_STRING;
            return;
        }
        String helpString = metricSamples.get(index - 1);
        if (!helpString.startsWith("# HELP " + metricName + " ")) {
            this.metricHelp = PrometheusConstants.EMPTY_STRING;
            return;
        }
        String[] metricHelpArray = helpString.split(" ", 4);
        this.metricHelp = metricHelpArray[metricHelpArray.length - 1];
    }

    private void generateMaps(List<String> retrievedMetrics) {
        for (String sampleSingleLine : retrievedMetrics) {
            Map<String, Object> metricMap = new LinkedHashMap<>();
            metricMap.put(PrometheusConstants.MAP_NAME, metricName);
            metricMap.put(PrometheusConstants.MAP_TYPE, MetricType.getMetricTypeString(metricType));
            metricMap.put(PrometheusConstants.MAP_HELP, metricHelp);

            String sampleName = sampleSingleLine.substring(0, sampleSingleLine.indexOf("{"));
            Object value = setMetricValue(sampleSingleLine.substring(sampleSingleLine.indexOf("}") + 1).trim());
            Map<String, String> labelValueMap = filterMetric(sampleSingleLine);
            if (sampleName.equals(metricName)) {
                metricMap.put(PrometheusConstants.MAP_SAMPLE_SUBTYPE, PrometheusConstants.SUBTYPE_NULL);
            }
            if (sampleName.equals(metricName + PrometheusConstants.BUCKET_POSTFIX)) {
                metricMap.put(PrometheusConstants.MAP_SAMPLE_SUBTYPE, PrometheusConstants.SUBTYPE_BUCKET);
            }
            if (sampleName.equals(metricName + PrometheusConstants.COUNT_POSTFIX)) {
                metricMap.put(PrometheusConstants.MAP_SAMPLE_SUBTYPE, PrometheusConstants.SUBTYPE_COUNT);
                addLeAndQuantileKeys(labelValueMap);
            }
            if (sampleName.equals(metricName + PrometheusConstants.SUM_POSTFIX)) {
                metricMap.put(PrometheusConstants.MAP_SAMPLE_SUBTYPE, PrometheusConstants.SUBTYPE_SUM);
                addLeAndQuantileKeys(labelValueMap);
            }
            for (Map.Entry<String, String> entry : labelValueMap.entrySet()) {
                metricMap.put(entry.getKey(), entry.getValue());
            }
            metricMap.put(PrometheusConstants.MAP_SAMPLE_VALUE, value);
            handleEvent(metricMap);
        }
    }

    private Object setMetricValue(String valueString) {
        switch (valueType) {
            case INT: {
                valueString = valueString.substring(0, valueString.indexOf("."));
                return Integer.parseInt(valueString);
            }
            case LONG: {
                valueString = valueString.substring(0, valueString.indexOf("."));
                return Long.parseLong(valueString);
            }
            case FLOAT: {
                return Float.parseFloat(valueString);
            }
            case DOUBLE: {
                return Double.parseDouble(valueString);
            }
            default: {
                //default will never be executed
                return null;
            }
        }
    }

    private void addLeAndQuantileKeys(Map<String, String> labelValueMap) {
        switch (metricType) {
            case HISTOGRAM:
                labelValueMap.put(PrometheusConstants.LE_KEY, PrometheusConstants.EMPTY_STRING);
                break;
            case SUMMARY:
                labelValueMap.put(PrometheusConstants.QUANTILE_KEY, PrometheusConstants.EMPTY_STRING);
                break;
            default:
                //default will never be executed.
        }
    }

    private void handleEvent(Map<String, Object> metricMap) {
        sourceEventListener.onEvent(metricMap, null);
    }

    /**
     * This method analyses a single line of the metric response and returns a label-value map
     */
    private Map<String, String> filterMetric(String metricSample) {
        String[] labelList = metricSample.substring(metricSample.indexOf("{") + 1, metricSample.indexOf("}"))
                .split(",");
        Map<String, String> labelMap = new LinkedHashMap<>();
        Arrays.stream(labelList).forEach(labelEntry -> {
            String[] entry = labelEntry.split("=");
            if (entry.length == 2) {
                String label = entry[0];
                String value = entry[1].substring(1, entry[1].length() - 1);
                labelMap.put(label, value);
            }
        });
        return labelMap;
    }

    private boolean checkMetricType(List<String> metricSamples, int index) {
        String[] metricTypeArray = metricSamples.get(index).split(" ", 4);
        String metricTypeResponse = metricTypeArray[metricTypeArray.length - 1];
        return metricTypeResponse.equalsIgnoreCase(MetricType.getMetricTypeString(metricType));
    }

    List<String> getLastValidSamples() {
        return this.lastValidSample;
    }

}
