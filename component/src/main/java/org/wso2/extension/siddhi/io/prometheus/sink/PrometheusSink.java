/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.extension.siddhi.io.prometheus.util.PrometheusUtil;
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
        description = "The sink publishes events processed by WSO2 SP into Prometheus metrics and expose \n" +
                "them to Prometheus server at the provided url. The created metrics can be published to \n" +
                "Prometheus through 'server' or 'pushGateway' publishing modes according to user preference.\n" +
                "The server mode exposes the metrics through an http server at the provided url and the \n" +
                " pushGateway mode pushes the metrics to pushGateway which must be running at the \n" +
                "provided url. The metric types that are supported by Prometheus sink are counter, gauge,\n" +
                "histogram and summary. And the values and labels of the Prometheus metrics can be updated \n" +
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
                                "The mode can be either \'server\' or \'pushgateway\'.",
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
                        description = "This parameter specifies the url where the http server will be initiated " +
                                "to expose metrics. This url must be previously defined in prometheus " +
                                "configuration file as a target. By default, the http server will be initiated at" +
                                "\'http://localhost:9080\'",
                        optional = true,
                        defaultValue = "http://localhost:9080",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "metric.type",
                        description = "The type of Prometheus metric that has to be created at the sink. " +
                                "The supported metric types are \'counter\', \'gauge\'," +
                                " \'histogram\' and \'summary\'. ",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "metric.help",
                        description = "A brief description of the metric and its purpose." +
                                " By default, the help string " +
                                "will be a combination of the metric name and its type.",
                        optional = true,
                        defaultValue = "metric name with metric type",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "metric.name",
                        description = "This parameter specifies the user preferred name for the metric. By default, " +
                                "metric name will be set according to the name of the stream. The metric name must " +
                                "match the regex format [a-zA-Z_:][a-zA-Z0-9_:]* ",
                        optional = true,
                        defaultValue = "stream name",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "buckets",
                        description = "The user preferred bucket values for histogram metrics. The bucket values " +
                                "must be in string format with each bucket value separated by a comma." +
                                "Expected format of the parameter is as follows: \n" +
                                "\"2,4,6,8\"",
                        optional = true,
                        defaultValue = "null",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "quantiles",
                        description = "The user preferred quantile values for summary metrics. The quantile values " +
                                "must be in string format with each quantile value separated by a comma." +
                                "Expected format of the parameter is as follows: \n" +
                                "\"0.5,0.75,0.95\"",
                        optional = true,
                        defaultValue = "null",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "quantile.error",
                        description = "The error tolerance value for calculating quantiles in summary metrics. " +
                                "This must be a positive value less than 1." +
                                " By default, the error tolerance will be 0.001.",
                        optional = true,
                        defaultValue = "0.001",
                        type = {DataType.DOUBLE}
                ),
                @Parameter(
                        name = "value.attribute",
                        description = "The name of the attribute in stream definition which specifies the metric " +
                                "value. The defined value attribute must be included inside the stream attributes. \n" +
                                "The value of the value attribute that published through events will increase the" +
                                " metric value for counter and gauge metric types. And for histogram and " +
                                "summary metric types, the values will be observed." +
                                " By default, the value attribute is specified as \'value\' ",
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
                                "only updates the newly created metrics. BY default, the push operation is " +
                                "assigned to  \'pushadd\'.",
                        optional = true,
                        defaultValue = "pushadd",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "grouping.key",
                        description = "This parameter specifies the grouping key of created metrics in key-value " +
                                "pairs. Grouping key is used only in pushGateway mode in order to distinguish the " +
                                "metrics from already existing metrics. The expected format of the grouping key" +
                                " is as follows: " +
                                "\"'key1:value1','key2:value2'\"",
                        optional = true,
                        defaultValue = "null",
                        type = {DataType.STRING}
                )
        },
        examples = {
                @Example(
                        syntax =
                                "@sink(type='prometheus',job='fooOrderCount', server.url ='http://localhost:9080',\n " +
                                        "publish.mode='server', metric.type='counter', \n" +
                                        "metric.help= 'Number of foo orders', @map(type='keyvalue'))\n" +
                                        "define stream FooCountStream (Name String, quantity int, value int);\n",
                        description = " In the above example, the Prometheus-sink will create a counter metric " +
                                "with the Stream name and defined attributes as labels. \n The metric will be exposed" +
                                " through an http server at the target url."
                ),
                @Example(
                        syntax =
                                "@sink(type='prometheus',job='inventoryLevel', push.url='http://localhost:9080',\n " +
                                        "publish.mode='pushGateway', metric.type='gauge',\n" +
                                        " metric.help= 'Current level of inventory', @map(type='keyvalue'))\n" +
                                        "define stream InventoryLevelStream (Name String, value int);\n",
                        description = " In the above example, the Prometheus-sink will create a gauge metric " +
                                "with the Stream name and defined attributes as labels.\n" +
                                "The metric will be pushed to Prometheus pushGateway at the target url."
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
                                "\"'key1:value1','key2:value2'\"",
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
        if (!optionHolder.isOptionExists(PrometheusConstants.METRIC_TYPE)) {
            throw new SiddhiAppCreationException("mandatory field \'metric.type\' is not found in sink configuration");
        }

        //check for custom mapping
        List<Annotation> annotations = outputstreamDefinition.getAnnotations();
        for (Annotation annotation : annotations) {
            List<Annotation> mapAnnotation = annotation.getAnnotations(PrometheusConstants.MAP_ANNOTATION);
            for (Annotation annotationMap : mapAnnotation) {
                if (!annotationMap.getAnnotations(PrometheusConstants.PAYLOAD_ANNOTATION).isEmpty()) {
                    throw new SiddhiAppCreationException("Unsupported mapping");
                }
            }
        }

        this.configReader = configReader;
        this.jobName = optionHolder.validateAndGetStaticValue(PrometheusConstants.JOB_NAME,
                PrometheusUtil.jobName(configReader));
        this.pushURL = optionHolder.validateAndGetStaticValue(PrometheusConstants.PUSH_URL,
                PrometheusUtil.pushURL(configReader));
        this.serverURL = optionHolder.validateAndGetStaticValue(PrometheusConstants.SERVER_URL,
                PrometheusUtil.serverURL(configReader));
        this.publishMode = optionHolder.validateAndGetStaticValue(PrometheusConstants.METRIC_PUBLISH_MODE,
                PrometheusUtil.publishMode(configReader));
        this.metricType = PrometheusUtil.assignMetricType(optionHolder.validateAndGetStaticValue(METRIC_TYPE));
        this.metricHelp = optionHolder.validateAndGetStaticValue(PrometheusConstants.METRIC_HELP,
                HELP_STRING + metricType + SPACE_STRING + metricName).trim();
        this.buckets = optionHolder.validateAndGetStaticValue(PrometheusConstants.BUCKET_DEFINITION, EMPTY_STRING);
        this.quantiles = optionHolder.validateAndGetStaticValue(PrometheusConstants.QUANTILES_DEFINITION, EMPTY_STRING);
        this.attributes = outputstreamDefinition.getAttributeList()
                .stream().map(Attribute::getName).collect(Collectors.toList());
        this.metricName = optionHolder.validateAndGetStaticValue(
                PrometheusConstants.METRIC_NAME, outputstreamDefinition.getId()).trim();
        this.pushOperation = optionHolder.validateAndGetStaticValue(
                PrometheusConstants.PUSH_DEFINITION, PrometheusConstants.PUSH_ADD_OPERATION).trim();
        this.groupingKey = PrometheusUtil.populateGroupingKey(optionHolder.validateAndGetStaticValue(
                PrometheusConstants.GROUPING_KEY_DEFINITION, PrometheusUtil.groupinKey(configReader)).trim());
        this.valueAttribute = optionHolder.validateAndGetStaticValue(
                PrometheusConstants.VALUE_ATTRIBUTE, VALUE_STRING).trim();
        try {
            this.quantileError = parseDouble(optionHolder.validateAndGetStaticValue(
                    PrometheusConstants.QUANTILE_ERROR, DEFAULT_ERROR));
            if (quantileError < 0 || quantileError >= 1.0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new SiddhiAppCreationException("Invalid value for \'quantile.error\'");
        }

        if (!publishMode.equalsIgnoreCase(SERVER_PUBLISH_MODE) &&
                !publishMode.equalsIgnoreCase(PUSHGATEWAY_PUBLISH_MODE)) {
            throw new SiddhiAppCreationException("Invalid publish mode : " + publishMode);
        }

        if (!metricName.matches(PrometheusConstants.METRIC_NAME_REGEX)) {
            throw new SiddhiAppCreationException("Invalid format of metric name: " + metricName);
        }

        if (!pushOperation.equalsIgnoreCase(PUSH_OPERATION) &&
                !pushOperation.equalsIgnoreCase(PUSH_ADD_OPERATION)) {
            throw new SiddhiAppCreationException("Invalid value for push operation : " + pushOperation);
        }

        // checking for value attribute and its type in stream definintion
        try {
            Attribute.Type valueType = outputstreamDefinition.getAttributeType(valueAttribute);
            if (valueType.equals(Attribute.Type.STRING) || valueType.equals(Attribute.Type.BOOL) ||
                    valueType.equals(Attribute.Type.OBJECT)) {
                throw new SiddhiAppCreationException("Invalid type for " + valueAttribute + " attribute");
            }
        } catch (AttributeNotExistException exception) {
            throw new SiddhiAppCreationException("The value attribute \'" + valueAttribute + "\' is not found " +
                    "in stream definition.");
        }

        // checking unsupported metric types for 'buckets'
        if (!buckets.isEmpty()) {
            if (metricType.equals(Collector.Type.COUNTER) ||
                    metricType.equals(Collector.Type.GAUGE) || metricType.equals(Collector.Type.SUMMARY)) {
                throw new SiddhiAppCreationException("Unsupported metric type for buckets");
            }
        }
        // checking unsupported metric types for 'quantiles' and unsupported values for quantiles
        if (!quantiles.isEmpty()) {
            if (metricType.equals(Collector.Type.COUNTER) ||
                    metricType.equals(Collector.Type.GAUGE) || metricType.equals(Collector.Type.HISTOGRAM)) {
                throw new SiddhiAppCreationException("unsupported metric type for quantiles");
            }
        }
        prometheusMetricBuilder = new PrometheusMetricBuilder(metricName, metricHelp, metricType, attributes);
        prometheusMetricBuilder.setHistogramBuckets(PrometheusUtil.convertToDoubleArray(buckets.trim()));
        double[] quantileValues = PrometheusUtil.convertToDoubleArray(quantiles.trim());
        if (PrometheusUtil.validateQuantiles(quantileValues)) {
            prometheusMetricBuilder.setQuantiles(quantileValues, quantileError);
        }
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
        Map<String, Object> attributeMap = (Map<String, Object>) payload;
        String[] labels;
        double value = parseDouble(attributeMap.get(valueAttribute).toString());
        labels = PrometheusUtil.populateLabelArray(attributeMap, valueAttribute);
        prometheusMetricBuilder.insertValues(value, labels);
        CollectorRegistry registry = prometheusMetricBuilder.getRegistry();

        if ((PrometheusConstants.PUSHGATEWAY_PUBLISH_MODE).equals(publishMode)) {
            try {
                switch (pushOperation) {
                    case PrometheusConstants.PUSH_OPERATION:
                        pushGateway.push(registry, jobName, groupingKey);
                        break;
                    case PrometheusConstants.PUSH_ADD_OPERATION:
                        pushGateway.pushAdd(registry, jobName, groupingKey);
                        break;
                    default:
                        //default will never be executed
                }
            } catch (IOException e) {
                log.error("Unable to establish connection", new ConnectionUnavailableException(e));
            }
        }
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        try {
            prometheusMetricBuilder.registerMetric(valueAttribute, publishMode);
            URL target;
            switch (publishMode) {
                case PrometheusConstants.SERVER_PUBLISH_MODE:
                    target = new URL(serverURL);
                    initiateServer(target.getHost(), target.getPort());
                    log.info(metricName + " has successfully connected at " + serverURL);
                    break;
                case PrometheusConstants.PUSHGATEWAY_PUBLISH_MODE:
                    target = new URL(pushURL);
                    pushGateway = new PushGateway(target);
                    try {
                        pushGateway.pushAdd(prometheusMetricBuilder.getRegistry(), jobName, groupingKey);
                        log.info(metricName + " has successfully connected to pushGateway at " + pushURL);
                    } catch (IOException e) {
                        if (e.getMessage().equalsIgnoreCase("Connection refused (Connection refused)")) {
                            log.error("Pushgateway is not listening at " + target,
                                    new ConnectionUnavailableException(e));
                        }
                    }
                    break;
                default:
                    //default will never be executed
            }
        } catch (MalformedURLException e) {
            throw new ConnectionUnavailableException("Error in URL " + e);
        }
    }

    private void initiateServer(String host, int port) {
        try {
            server = new HTTPServer(host, port);
        } catch (IOException e) {
            if (!(e instanceof BindException && e.getMessage().equals("Address already in use"))) {
                log.error("Unable to establish connection ", new ConnectionUnavailableException(e));
            }
        }
    }

    @Override
    public void disconnect() {
        if (server != null) {
            server.stop();
            log.info("server stopped successfully at " + serverURL);
        }
    }

    @Override
    public void destroy() {
        CollectorRegistry registry = prometheusMetricBuilder.getRegistry();
        if (registry != null) {
            registry.clear();
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

