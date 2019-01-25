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

import org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;

import java.util.Locale;

/**
 * This specifies the metric types that are supported by Prometheus
 */
public enum MetricType {

    COUNTER,
    GAUGE,
    SUMMARY,
    HISTOGRAM;

    public static MetricType assignMetricType(String metricTypeString,  String streamID) {
        MetricType metricType;
        switch (metricTypeString.trim().toUpperCase(Locale.ENGLISH)) {
            case "COUNTER": {
                metricType = MetricType.COUNTER;
                break;
            }
            case "GAUGE": {
                metricType = MetricType.GAUGE;
                break;
            }
            case "HISTOGRAM": {
                metricType = MetricType.HISTOGRAM;
                break;
            }
            case "SUMMARY": {
                metricType = MetricType.SUMMARY;
                break;
            }
            default: {
                throw new SiddhiAppCreationException("The \'metric.type\' field in " +
                        PrometheusConstants.PROMETHEUS_SOURCE + " associated " +
                        "with stream \'" + streamID + "\' contains illegal value");
            }
        }
        return metricType;
    }

    public static String getMetricTypeString(MetricType metricType) {
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
                //default will never be executed
                return null;
            }
        }
    }
}

