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

package org.wso2.extension.siddhi.io.prometheus.util;


import io.prometheus.client.Collector;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.KEY_VALUE_SEPARATOR;
import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.VALUE_SEPARATOR;


/**
 * {@code PrometheusSinkUtil } responsible for util functions of Prometheus-sink.
 */

public class PrometheusSinkUtil {


    /**
     * Split values by ',' for buckets and quantiles definition.
     *
     * @param inputString string input from sink definition
     * @param streamID    streamId of the stream for error message
     * @return value list as double array
     */
    public static double[] convertToDoubleArray(String inputString, String streamID) {
        if (!PrometheusConstants.EMPTY_STRING.equals(inputString)) {
            List<String> stringList = Arrays.asList(inputString.split(PrometheusConstants.ELEMENT_SEPARATOR));

            try {
                return stringList.stream().mapToDouble(Double::parseDouble).toArray();
            } catch (NumberFormatException e) {
                throw new SiddhiAppCreationException("The buckets/quantiles field in Prometheus sink associated " +
                        "with the stream \'" + streamID + "\' is not in the expected format. " +
                        "please insert the numerical values as \"2,3,4,5\".");
            }
        } else {
            return new double[0];
        }
    }

    /**
     * Validate quantile values to be in between 0 and 1 for summary metric type.
     *
     * @param quantiles quantile values as double array
     * @param streamID  streamId of the stream for error message
     */
    public static boolean validateQuantiles(double[] quantiles, String streamID) {
        for (double value : quantiles) {
            if ((value < 0) || (value > 1.0)) {
                throw new SiddhiAppCreationException("The values assigned for quantiles in Prometheus sink associated" +
                        " with stream \'" + streamID + "\' are invalid." +
                        "Please insert values between 0 and 1.");
            }
        }
        return true;
    }

    /**
     * Assign metric types according to sink definition.
     *
     * @param metricTypeString value of metric type parameter from sink definition
     * @param streamID         streamId of the stream for error message
     * @return Metric type from Prometheus Collector
     */
    public static Collector.Type assignMetricType(String metricTypeString, String streamID) {
        Collector.Type metricType;
        switch (metricTypeString.trim().toUpperCase(Locale.ENGLISH)) {
            case "COUNTER": {
                metricType = Collector.Type.COUNTER;
                break;
            }
            case "GAUGE": {
                metricType = Collector.Type.GAUGE;
                break;
            }
            case "HISTOGRAM": {
                metricType = Collector.Type.HISTOGRAM;
                break;
            }
            case "SUMMARY": {
                metricType = Collector.Type.SUMMARY;
                break;
            }
            default: {
                throw new SiddhiAppCreationException("The \'metric.type\' field in Prometheus sink associated " +
                        "with stream \'" + streamID + "\' contains illegal value");
            }
        }
        return metricType;
    }

    /**
     * Retrieve grouping key of a metric as key-value pair.
     *
     * @param groupingKeyString grouping key parameter as string
     * @param streamID          streamId of the stream for error message
     * @return key-value pairs of the grouping key as java string map
     */
    public static Map<String, String> populateGroupingKey(String groupingKeyString, String streamID) {
        Map<String, String> groupingKey = new HashMap<>();
        if (PrometheusConstants.EMPTY_STRING.equals(groupingKeyString)) {
            return groupingKey;
        }
        String[] keyList = groupingKeyString.substring(1, groupingKeyString.length() - 1)
                .split(KEY_VALUE_SEPARATOR);
        Arrays.stream(keyList).forEach(valueEntry -> {
            String[] entry = valueEntry.split(VALUE_SEPARATOR);
            if (entry.length != 2) {
                throw new SiddhiAppCreationException("The grouping key field in Prometheus sink associated " +
                        "with the stream \'" + streamID + "\' is not in the expected format. " +
                        "please insert them as 'key1:val1','key2:val2'.");
            }
            groupingKey.put(entry[0], entry[1]);

        });
        return groupingKey;
    }

    /**
     * Retrieve labels from payload to assign metrics.
     *
     * @param attributeMap   payload in Map format
     * @param valueAttribute name of the value attribute
     * @return labels as string array
     */
    public static String[] populateLabelArray(Map<String, Object> attributeMap, String valueAttribute) {
        String[] labels = new String[attributeMap.size() - 1];
        int count = 0;
        for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
            if (entry.getKey().equals(valueAttribute)) {
                continue;
            }
            labels[count] = entry.getValue().toString();
            count++;
        }
        return labels;
    }

    /**
     * user can give custom job name if user did not define them. Then system will read
     * the default values which is in the deployment yaml.
     *
     * @param sinkConfigReader configuration reader for sink.
     * @return default job name.
     */
    public static String jobName(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(PrometheusConstants.JOB_NAME_CONFIGURATION,
                PrometheusConstants.DEFAULT_JOB_NAME);
    }

    /**
     * user can give custom URL for Prometheus push gateway if user did not give them inside sink definition. Then
     * system will read the default values which is in the deployment yaml.
     *
     * @param sinkConfigReader configuration reader for sink.
     * @return default push gateway URL.
     */
    public static String pushURL(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(PrometheusConstants.PUSH_URL_CONFIGURATION,
                PrometheusConstants.DEFAULT_PUSH_URL);
    }

    /**
     * user can give custom server URL if user did not give them inside sink definition. Then system will read
     * the default values which is in the deployment yaml.
     *
     * @param sinkConfigReader configuration reader for sink.
     * @return default server URL.
     */
    public static String serverURL(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(PrometheusConstants.SERVER_URL_CONFIGURATION,
                PrometheusConstants.DEFAULT_SERVER_URL);
    }

    /**
     * user can give custom publish mode if the user did not give them inside sink definition then system read
     * the default values which is in the deployment yaml.
     *
     * @param sinkConfigReader configuration reader for sink.
     * @return default publish mode.
     */
    public static String publishMode(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(PrometheusConstants.PUBLISH_MODE_CONFIGURATION,
                PrometheusConstants.DEFAULT_PUBLISH_MODE);
    }

    /**
     * user can give custom grouping key in key-value pairs, if user did not give them inside sink definition.
     * Then system will read the default values which is in the deployment yaml.
     *
     * @param sinkConfigReader configuration reader for sink.
     * @return default grouping key.
     */
    public static String groupinKey(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(PrometheusConstants.GROUPING_KEY_CONFIGURATION,
                PrometheusConstants.EMPTY_STRING);
    }

    /**
     * To retrieve the name of the metric type in String from {@code Collector.Type}
     *
     * @param metricType metric type from {@code Collector.Type}
     * @return name of the metric type
     */
    public static String getMetricTypeString(Collector.Type metricType) {
        switch (metricType) {
            case COUNTER: {
                return "counter";
            }
            case GAUGE: {
                return "gauge";
            }
            case HISTOGRAM: {
                return "histogram";
            }
            case SUMMARY: {
                return "summary";
            }
            default: {
                return null;
                // default will never be executed
            }
        }
    }
}
