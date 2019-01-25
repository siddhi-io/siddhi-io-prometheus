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

package org.wso2.extension.siddhi.io.prometheus.sink;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.exporter.PushGateway;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.prometheus.sink.util.PrometheusMetricBuilder;
import org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants;
import org.wso2.extension.siddhi.io.prometheus.util.PrometheusSinkUtil;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.SystemParameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.DEFAULT_ERROR;
import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.EMPTY_STRING;
import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.HELP_STRING;
import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.METRIC_TYPE;
import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.PUSHGATEWAY_PUBLISH_MODE;
import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.PUSH_ADD_OPERATION;
import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.PUSH_OPERATION;
import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.SERVER_PUBLISH_MODE;
import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.SPACE_STRING;
import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.VALUE_STRING;
import static java.lang.Double.parseDouble;

/**
 * Extension for Siddhi to publish events as Prometheus metrics.
 **/
@Extension(
        name = "prometheus",
        namespace = "sink",
        description = "The sink publishes events processed by WSO2 SP into Prometheus metrics and exposes " +
                "them to Prometheus server at the provided url. The created metrics can be published to " +
                "Prometheus through 'server' or 'pushGateway' publishing modes depending on the preference of the " +
                "user. The server mode exposes the metrics through an http server at the provided url and the " +
                "pushGateway mode pushes the metrics to pushGateway which must be running at the " +
                "provided url.\n The metric types that are supported by Prometheus sink are counter, gauge, " +
                "histogram and summary. The values and labels of the Prometheus metrics can be updated " +
                "through the events. ",
        parameters = {
                @Parameter(
                        name = "job",
                        description = "This parameter specifies the job name of the metric. The name must be " +
                                "the same job name as defined in the prometheus configuration file.",
                        defaultValue = "siddhiJob",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "publish.mode",
                        description = "This parameter specifies the mode of exposing metrics to Prometheus server." +
                                "The possible publishing modes are \'server\' and \'pushgateway\'.",
                        defaultValue = "server",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "push.url",
                        description = "This parameter specifies the target url of the Prometheus pushGateway " +
                                "where the pushGateway must be listening. This url should be previously " +
                                "defined in prometheus configuration file as a target.",
                        optional = true,
                        defaultValue = "http://localhost:9091",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "server.url",
                        description = "This parameter specifies the url where the http server is initiated " +
                                "to expose metrics for \'server\' publish mode. This url must be " +
                                "previously defined in prometheus configuration file as a target.",
                        optional = true,
                        defaultValue = "http://localhost:9080",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "metric.type",
                        description = "The type of Prometheus metric that has to be created at the sink.\n " +
                                "The supported metric types are \'counter\', \'gauge\'," +
                                " \'histogram\' and \'summary\'. ",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "metric.help",
                        description = "A brief description of the metric and its purpose.",
                        optional = true,
                        defaultValue = "<metric_name_with_metric_type>",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "metric.name",
                        description = "This parameter specifies the user preferred name for the metric. The metric " +
                                "name must match the regex format, i.e., [a-zA-Z_:][a-zA-Z0-9_:]*. ",
                        optional = true,
                        defaultValue = "<stream_name>",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "buckets",
                        description = "The bucket values preferred by the user for histogram metrics. The bucket " +
                                "values must be in 'string' format with each bucket value separated by a comma." +
                                "\nThe expected format of the parameter is as follows: \n" +
                                "\"2,4,6,8\"",
                        optional = true,
                        defaultValue = "null",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "quantiles",
                        description = "The user preferred quantile values for summary metrics. The quantile values " +
                                "must be in 'string' format with each quantile value separated by a comma." +
                                "\nThe expected format of the parameter is as follows: \n" +
                                "\"0.5,0.75,0.95\"",
                        optional = true,
                        defaultValue = "null",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "quantile.error",
                        description = "The error tolerance value for calculating quantiles in summary metrics. " +
                                "This must be a positive value though less than 1.",
                        optional = true,
                        defaultValue = "0.001",
                        type = {DataType.DOUBLE}
                ),
                @Parameter(
                        name = "value.attribute",
                        description = "The name of the attribute in stream definition which specifies the metric " +
                                "value. The defined value attribute must be included inside the stream attributes." +
                                " The value of the 'value' attribute that is published through events, increase the" +
                                " metric value for the counter and gauge metric types. For histogram and " +
                                "summary metric types, the values are observed.",
                        optional = true,
                        defaultValue = "value",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "push.operation",
                        description = "This parameter defines the mode for pushing metrics to pushGateway " +
                                "The available push operations are \'push\' and \'pushadd\'. " +
                                "The operations differ according to the existing metrics in pushGateway where " +
                                "\'push\' operation replaces the existing metrics and \'pushadd\' operation " +
                                "only updates the newly created metrics.",
                        optional = true,
                        defaultValue = "pushadd",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "grouping.key",
                        description = "This parameter specifies the grouping key of created metrics in key-value " +
                                "pairs. Grouping key is used only in pushGateway mode in order to distinguish the " +
                                "metrics from already existing metrics. \nThe expected format of the grouping key" +
                                " is as follows:\n " +
                                "\"'key1:value1','key2:value2'\"",
                        optional = true,
                        defaultValue = "<empty_string>",
                        type = {DataType.STRING}
                )
        },
        examples = {
                @Example(
                        syntax =
                                "@sink(type='prometheus',job='fooOrderCount', server.url ='http://localhost:9080', " +
                                        "publish.mode='server', metric.type='counter', " +
                                        "metric.help= 'Number of foo orders', @map(type='keyvalue'))\n" +
                                        "define stream FooCountStream (Name String, quantity int, value int);\n",
                        description = " In the above example, the Prometheus-sink creates a counter metric " +
                                "with the stream name and defined attributes as labels. The metric is exposed" +
                                " through an http server at the target url."
                ),
                @Example(
                        syntax =
                                "@sink(type='prometheus',job='inventoryLevel', push.url='http://localhost:9080', " +
                                        "publish.mode='pushGateway', metric.type='gauge'," +
                                        " metric.help= 'Current level of inventory', @map(type='keyvalue'))\n" +
                                        "define stream InventoryLevelStream (Name String, value int);\n",
                        description = " In the above example, the Prometheus-sink creates a gauge metric " +
                                "with the stream name and defined attributes as labels." +
                                "The metric is pushed to Prometheus pushGateway at the target url."
                )
        },
        systemParameter = {
                @SystemParameter(
                        name = "jobName",
                        description = "This is the property that specifies the default job name for the metric. " +
                                "The name must be the same job name as defined in the prometheus configuration file.",
                        defaultValue = "siddhiJob",
                        possibleParameters = "Any string"
                ),
                @SystemParameter(
                        name = "publishMode",
                        description = "The default publish mode for the Prometheus sink for exposing metrics to" +
                                " Prometheus server. The mode can be either \'server\' or \'pushgateway\'. ",
                        defaultValue = "server",
                        possibleParameters = "server or pushgateway"
                ),
                @SystemParameter(
                        name = "serverURL",
                        description = "This property configures the url where the http server will be initiated " +
                                "to expose metrics. This url must be previously defined in prometheus " +
                                "configuration file as a target to be identified by Prometheus. By default, " +
                                "the http server will be initiated at \'http://localhost:9080\'",
                        defaultValue = "http://localhost:9080",
                        possibleParameters = "Any valid URL"
                ),
                @SystemParameter(
                        name = "pushURL",
                        description = "This property configures the target url of Prometheus pushGateway " +
                                "where the pushGateway must be listening. This url should be previously " +
                                "defined in prometheus configuration file as a target to be identified by Prometheus.",
                        defaultValue = "http://localhost:9091",
                        possibleParameters = "Any valid URL"
                ),
                @SystemParameter(
                        name = "groupingKey",
                        description = "This property configures the grouping key of created metrics in key-value " +
                                "pairs. Grouping key is used only in pushGateway mode in order to distinguish the " +
                                "metrics from already existing metrics under same job. " +
                                "The expected format of the grouping key is as follows: " +
                                "\"'key1:value1','key2:value2'\" .",
                        defaultValue = "null",
                        possibleParameters = "Any key value pairs in the supported format"
                )
        }
)

public class PrometheusSink extends Sink {
    private static final Logger log = Logger.getLogger(PrometheusSink.class);

    private String jobName;
    private String pushURL;
    private String serverURL;
    private String publishMode;
    private Collector.Type metricType;
    private String metricHelp;
    private String metricName;
    private List<String> attributes;
    private String buckets;
    private String quantiles;
    private String pushOperation;
    private Map<String, String> groupingKey;
    private String valueAttribute;
    private double quantileError;

    private PrometheusMetricBuilder prometheusMetricBuilder;
    private HTTPServer server;
    private PushGateway pushGateway;
    private CollectorRegistry collectorRegistry;
    private String registeredMetrics;
    private ConfigReader configReader;

    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{Map.class};
    }

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[0];
    }

    @Override
    protected void init(StreamDefinition outputstreamDefinition, OptionHolder optionHolder, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        String streamID = outputstreamDefinition.getId();
        if (!optionHolder.isOptionExists(PrometheusConstants.METRIC_TYPE)) {
            throw new SiddhiAppCreationException("The mandatory field \'metric.type\' is not found in Prometheus " +
                    "sink associated with stream \'" + streamID + " \'");
        }
        //check for custom mapping
        List<Annotation> annotations = outputstreamDefinition.getAnnotations();
        for (Annotation annotation : annotations) {
            List<Annotation> mapAnnotation = annotation.getAnnotations(PrometheusConstants.MAP_ANNOTATION);
            for (Annotation annotationMap : mapAnnotation) {
                if (!annotationMap.getAnnotations(PrometheusConstants.PAYLOAD_ANNOTATION).isEmpty()) {
                    throw new SiddhiAppCreationException("Custom mapping associated with stream \'" +
                            streamID + "\' is not supported by Prometheus sink");
                }
            }
        }
        this.configReader = configReader;
        this.jobName = optionHolder.validateAndGetStaticValue(PrometheusConstants.JOB_NAME,
                PrometheusSinkUtil.jobName(configReader));
        this.pushURL = optionHolder.validateAndGetStaticValue(PrometheusConstants.PUSH_URL,
                PrometheusSinkUtil.pushURL(configReader));
        this.serverURL = optionHolder.validateAndGetStaticValue(PrometheusConstants.SERVER_URL,
                PrometheusSinkUtil.serverURL(configReader));
        this.publishMode = optionHolder.validateAndGetStaticValue(PrometheusConstants.METRIC_PUBLISH_MODE,
                PrometheusSinkUtil.publishMode(configReader));
        this.buckets = optionHolder.validateAndGetStaticValue(PrometheusConstants.BUCKET_DEFINITION, EMPTY_STRING);
        this.quantiles = optionHolder.validateAndGetStaticValue(PrometheusConstants.QUANTILES_DEFINITION, EMPTY_STRING);
        this.attributes = outputstreamDefinition.getAttributeList()
                .stream().map(Attribute::getName).collect(Collectors.toList());
        this.metricName = optionHolder.validateAndGetStaticValue(
                PrometheusConstants.METRIC_NAME, streamID.trim());
        this.metricType = PrometheusSinkUtil.assignMetricType(optionHolder.validateAndGetStaticValue(METRIC_TYPE),
                streamID);
        this.metricHelp = optionHolder.validateAndGetStaticValue(PrometheusConstants.METRIC_HELP,
                HELP_STRING + PrometheusSinkUtil.getMetricTypeString(metricType) + SPACE_STRING +
                        metricName).trim();
        this.pushOperation = optionHolder.validateAndGetStaticValue(
                PrometheusConstants.PUSH_DEFINITION, PrometheusConstants.PUSH_ADD_OPERATION).trim();
        this.groupingKey = PrometheusSinkUtil.populateGroupingKey(optionHolder.validateAndGetStaticValue(
                PrometheusConstants.GROUPING_KEY_DEFINITION, PrometheusSinkUtil.groupinKey(configReader)).trim(),
                streamID);
        this.valueAttribute = optionHolder.validateAndGetStaticValue(
                PrometheusConstants.VALUE_ATTRIBUTE, VALUE_STRING).trim();
        try {
            this.quantileError = parseDouble(optionHolder.validateAndGetStaticValue(
                    PrometheusConstants.QUANTILE_ERROR, DEFAULT_ERROR));
            if (quantileError < 0 || quantileError >= 1.0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new SiddhiAppCreationException("Invalid value for \'quantile.error\' in Prometheus sink " +
                    "associated with stream \'" + streamID + "\'. Value must be between 0 and 1");
        }

        if (!publishMode.equalsIgnoreCase(SERVER_PUBLISH_MODE) &&
                !publishMode.equalsIgnoreCase(PUSHGATEWAY_PUBLISH_MODE)) {
            throw new SiddhiAppCreationException("Invalid publish mode : " + publishMode + " in Prometheus sink " +
                    "associated with stream \'" + streamID + "\'.");
        }

        if (!metricName.matches(PrometheusConstants.METRIC_NAME_REGEX)) {
            throw new SiddhiAppCreationException("Metric name \'" + metricName + "\' does not match the regex " +
                    "\"[a-zA-Z_:][a-zA-Z0-9_:]*\" in Prometheus sink associated with stream \'"
                    + streamID + "\'.");
        }

        if (!pushOperation.equalsIgnoreCase(PUSH_OPERATION) &&
                !pushOperation.equalsIgnoreCase(PUSH_ADD_OPERATION)) {
            throw new SiddhiAppCreationException("Invalid value for push operation : " + pushOperation +
                    " in Prometheus sink associated with stream \'" + streamID + "\'.");
        }

        // checking for value attribute and its type in stream definintion
        try {
            Attribute.Type valueType = outputstreamDefinition.getAttributeType(valueAttribute);
            if (valueType.equals(Attribute.Type.STRING) || valueType.equals(Attribute.Type.BOOL) ||
                    valueType.equals(Attribute.Type.OBJECT)) {
                throw new SiddhiAppCreationException("The field value attribute \'" + valueAttribute + " \'contains " +
                        "unsupported type in Prometheus sink associated with stream \'" + streamID + "\'");
            }
        } catch (AttributeNotExistException exception) {
            throw new SiddhiAppCreationException("The value attribute \'" + valueAttribute + "\' is not found " +
                    "in Prometheus sink associated with stream \'" + streamID + "\'");
        }

        // checking unsupported metric types for 'buckets'
        if (!buckets.isEmpty()) {
            if (metricType.equals(Collector.Type.COUNTER) ||
                    metricType.equals(Collector.Type.GAUGE) || metricType.equals(Collector.Type.SUMMARY)) {
                throw new SiddhiAppCreationException("The buckets field in Prometheus sink associated with stream \'" +
                        streamID + "\' is not supported " +
                        "for metric type \'" + metricType + "\'.");
            }
        }
        // checking unsupported metric types for 'quantiles' and unsupported values for quantiles
        if (!quantiles.isEmpty()) {
            if (metricType.equals(Collector.Type.COUNTER) ||
                    metricType.equals(Collector.Type.GAUGE) || metricType.equals(Collector.Type.HISTOGRAM)) {
                throw new SiddhiAppCreationException("The quantiles field in Prometheus sink associated with " +
                        "stream \'" + streamID + "\' is not supported " +
                        "for metric type \'" + metricType + "\'.");
            }
        }
        prometheusMetricBuilder = new PrometheusMetricBuilder(metricName, metricHelp, metricType, attributes);
        prometheusMetricBuilder.setHistogramBuckets(PrometheusSinkUtil.convertToDoubleArray(buckets.trim(), streamID));
        double[] quantileValues = PrometheusSinkUtil.convertToDoubleArray(quantiles.trim(), streamID);
        if (PrometheusSinkUtil.validateQuantiles(quantileValues, streamID)) {
            prometheusMetricBuilder.setQuantiles(quantileValues, quantileError);
        }
        switch (publishMode) {
            case PrometheusConstants.SERVER_PUBLISH_MODE:
                collectorRegistry = prometheusMetricBuilder.setRegistry(serverURL);
                break;
            case PrometheusConstants.PUSHGATEWAY_PUBLISH_MODE:
                collectorRegistry = prometheusMetricBuilder.setRegistry(pushURL);
                break;
            default:
                //default execution is not needed
        }
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
        Map<String, Object> attributeMap = (Map<String, Object>) payload;
        String[] labels;
        double value = parseDouble(attributeMap.get(valueAttribute).toString());
        labels = PrometheusSinkUtil.populateLabelArray(attributeMap, valueAttribute);
        prometheusMetricBuilder.insertValues(value, labels);
        if ((PrometheusConstants.PUSHGATEWAY_PUBLISH_MODE).equals(publishMode)) {
            try {
                switch (pushOperation) {
                    case PrometheusConstants.PUSH_OPERATION:
                        pushGateway.push(collectorRegistry, jobName, groupingKey);
                        break;
                    case PrometheusConstants.PUSH_ADD_OPERATION:
                        pushGateway.pushAdd(collectorRegistry, jobName, groupingKey);
                        break;
                    default:
                        //default will never be executed
                }
            } catch (IOException e) {
                log.error("Unable to establish connection for Prometheus sink associated with " +
                                "stream \'" + getStreamDefinition().getId() + "\' at " + pushURL,
                        new ConnectionUnavailableException(e));
            }
        }
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        try {
            URL target;
            switch (publishMode) {
                case PrometheusConstants.SERVER_PUBLISH_MODE:
                    target = new URL(serverURL);
                    initiateServer(target.getHost(), target.getPort());
                    log.info(getStreamDefinition().getId() + " has successfully connected at " + serverURL);
                    break;
                case PrometheusConstants.PUSHGATEWAY_PUBLISH_MODE:
                    target = new URL(pushURL);
                    pushGateway = new PushGateway(target);
                    try {
                        pushGateway.pushAdd(collectorRegistry, jobName, groupingKey);
                        log.info(getStreamDefinition().getId() + " has successfully connected to pushGateway at "
                                + pushURL);
                    } catch (IOException e) {
                        if (e.getMessage().equalsIgnoreCase("Connection refused (Connection refused)")) {
                            log.error("The stream \'" + getStreamDefinition().getId() + "\' of Prometheus sink " +
                                            "could not connect to Pushgateway." +
                                            " Prometheus pushgateway is not listening at " + target,
                                    new ConnectionUnavailableException(e));
                        }
                    }
                    break;
                default:
                    //default will never be executed
            }
            prometheusMetricBuilder.registerMetric(valueAttribute);
        } catch (MalformedURLException e) {
            throw new ConnectionUnavailableException("Error in URL format in Prometheus sink associated with stream \'"
                    + getStreamDefinition().getId() + "\'. \n " + e);
        }
    }

    private void initiateServer(String host, int port) {
        try {
            InetSocketAddress address = new InetSocketAddress(host, port);
            server = new HTTPServer(address, collectorRegistry);
        } catch (IOException e) {
            if (!(e instanceof BindException && e.getMessage().equals("Address already in use"))) {
                log.error("Unable to establish connection for Prometheus sink associated with stream \'" +
                                getStreamDefinition().getId() + "\' at " + serverURL,
                        new ConnectionUnavailableException(e));
            }
        }
    }

    @Override
    public void disconnect() {
        if (server != null) {
            server.stop();
            log.info("Server successfully stopped at " + serverURL);
        }
    }

    @Override
    public void destroy() {
        if (collectorRegistry != null) {
            collectorRegistry.clear();
        }
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> currentMetrics = new HashMap<>();
        currentMetrics.put(PrometheusConstants.REGISTERED_METRICS, assignRegisteredMetrics());
        return currentMetrics;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
        Object currentMetricSample = map.get(PrometheusConstants.REGISTERED_METRICS);
        if (!currentMetricSample.equals(EMPTY_STRING)) {
            registeredMetrics = currentMetricSample.toString();
        }
    }

    private String assignRegisteredMetrics() {
        Enumeration<Collector.MetricFamilySamples> registeredMetricSamples = prometheusMetricBuilder.
                getRegistry().metricFamilySamples();
        Collector.MetricFamilySamples metricFamilySamples;
        while (registeredMetricSamples.hasMoreElements()) {
            metricFamilySamples = registeredMetricSamples.nextElement();
            if (metricFamilySamples.name.equals(metricName)) {
                return metricFamilySamples.toString();
            }
        }
        return "";
    }
}

