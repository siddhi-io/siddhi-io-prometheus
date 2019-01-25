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

package org.wso2.extension.siddhi.io.prometheus.sink.util;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.SimpleCollector;
import io.prometheus.client.SimpleCollector.Builder;
import io.prometheus.client.Summary;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


/**
 * {@code PrometheusMetricBuilder } Builds and assigns values for metrics.
 */
public class PrometheusMetricBuilder {

    private CollectorRegistry registry;
    private String metricName;
    private String metricHelp;
    private List<String> attributes;
    private Collector.Type metricType;
    private Collector metricsCollector;
    private double[] histogramBuckets = new double[0];
    private double[] summaryQuantiles = new double[0];
    private double quantileError;

    public final CollectorRegistry getRegistry() {
        return registry;
    }

    public PrometheusMetricBuilder(String metricName, String metrichelp,
                                   Collector.Type metricType, List<String> labels) {
        this.metricName = metricName;
        this.metricHelp = metrichelp;
        this.metricType = metricType;
        this.attributes = labels;
    }

    public void setHistogramBuckets(double[] histogramBuckets) {
        this.histogramBuckets = histogramBuckets.clone();
    }

    public void setQuantiles(double[] summaryQuantiles, Double quantileError) {
        this.summaryQuantiles = summaryQuantiles.clone();
        this.quantileError = quantileError;
    }

    public void registerMetric(String valueAttribute) {
        metricsCollector = buildMetric(valueAttribute).register(registry);
    }

    private Builder buildMetric(String valueAttribute) {
        attributes.remove(valueAttribute);
        String[] metricLabels = attributes.toArray(new String[0]);

        Builder builder = new Builder() {
            @Override
            public SimpleCollector create() {
                return null;
            }
        };
        switch (metricType) {
            case COUNTER: {
                builder = Counter.build(metricName, metricHelp);
                break;
            }
            case GAUGE: {
                builder = Gauge.build(metricName, metricHelp);
                break;
            }
            case HISTOGRAM: {
                builder = Histogram.build(metricName, metricHelp);
                break;
            }
            case SUMMARY: {
                builder = Summary.build(metricName, metricHelp);
                break;
            }
            default: //default will never be executed
        }
        builder.labelNames(metricLabels);
        if (metricType == Collector.Type.HISTOGRAM) {
            if (!(histogramBuckets.length == 0)) {
                ((Histogram.Builder) builder).buckets(histogramBuckets);
            }
        }
        if (metricType == Collector.Type.SUMMARY) {
            if (!(summaryQuantiles.length == 0)) {
                for (double summaryQuantile : summaryQuantiles) {
                    ((Summary.Builder) builder).quantile(summaryQuantile, quantileError);
                }
            }
        }
        return builder;
    }

    //update values for metric labels
    public void insertValues(double value, String[] labelValues) {
        switch (metricType) {
            case COUNTER: {
                ((Counter) metricsCollector).labels(labelValues).inc(value);
                break;
            }
            case GAUGE: {
                ((Gauge) metricsCollector).labels(labelValues).inc(value);
                break;
            }
            case HISTOGRAM: {
                ((Histogram) metricsCollector).labels(labelValues).observe(value);
                break;
            }
            case SUMMARY: {
                ((Summary) metricsCollector).labels(labelValues).observe(value);
                break;
            }
            default: //default will never be executed
        }
    }

    public CollectorRegistry setRegistry(String url) {
        URL target;
        try {
            target = new URL(url);
            registry = PrometheusRegistryHolder.retrieveRegistry(target.getHost(), target.getPort());
        } catch (MalformedURLException e) {
            throw new SiddhiAppCreationException("Error in URL " + e);
        }
        return registry;
    }
}
