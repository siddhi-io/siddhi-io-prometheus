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

import org.wso2.carbon.messaging.Header;
import org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants;
import org.wso2.extension.siddhi.io.prometheus.util.PrometheusSourceUtil;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.SystemParameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.EMPTY_STRING;

/**
 * Extension for siddhi to retrieve Prometheus metrics from an http endpoint.
 **/
@Extension(
        name = "prometheus",
        namespace = "source",
        description = "The source consumes Prometheus metrics which are exported from the specified url as " +
                "Siddhi events, by making http requests to the url. According to the source configuration, it " +
                "analyses metrics from the text response and sends them as Siddhi events through key-value mapping." +
                "The user can retrieve metrics of types including, counter, gauge, histogram and summary. Since the" +
                " source retrieves the metrics from a text response of the target, it is advised to use \'string\' " +
                "as the attribute type for the attributes that correspond to Prometheus metric labels. Further, the" +
                " Prometheus metric value is passed through the event as 'value'. Therefore, it is advisable to " +
                "have an attribute with the name 'value' in the stream. \nThe supported types for the attribute, " +
                "'value' are INT, LONG, FLOAT and DOUBLE.",
        parameters = {
                @Parameter(name = "target.url",
                        description = "This property specifies the target url where the Prometheus metrics are " +
                                "exported in text format.",
                        type = DataType.STRING),
                @Parameter(
                        name = "scrape.interval",
                        description = "This property specifies the time interval in seconds within which the source " +
                                "should make an HTTP request to the  provided target url.",
                        defaultValue = "60",
                        optional = true,
                        type = {DataType.INT}
                ),
                @Parameter(
                        name = "scrape.timeout",
                        description = "This property is the time duration in seconds for a scrape request to get " +
                                "timed-out if the server at the url does not respond.",
                        defaultValue = "10",
                        optional = true,
                        type = {DataType.INT}
                ),
                @Parameter(
                        name = "scheme",
                        description = "This property specifies the scheme of the target URL.\n The supported schemes" +
                                " are 'HTTP' and 'HTTPS'.",
                        defaultValue = "HTTP",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "metric.name",
                        description = "This property specifies the name of the metrics that are to be fetched. The " +
                                "metric name must match the regex format, i.e., [a-zA-Z_:][a-zA-Z0-9_:]* .",
                        defaultValue = "Stream name",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "metric.type",
                        description = "This property specifies the type of the Prometheus metric that is required " +
                                "to be fetched. \n The supported metric types are \'counter\', \'gauge\'," +
                                "\" \'histogram\' and \'summary\'. ",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "username",
                        description = "This property specifies the username that has to be added in the authorization" +
                                " header of the HTTP request, if basic authentication is enabled at the target. It " +
                                "is required to specify both username and password to enable basic authentication. " +
                                "If one of the parameter is not given by user then an error is logged in the console.",
                        defaultValue = "<empty_string>",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "password",
                        description = "This property specifies the password that has to be added in the authorization" +
                                " header of the request, if the basic authentication is enabled at the target. It " +
                                "is required to specify both the username and password to enable basic authentication" +
                                ". If one of the parameter is not given by user, then an error is " +
                                "logged in the console.",
                        defaultValue = "<empty_string>",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "client.truststore.file",
                        description = "The file path to the location of the truststore to which the client needs to " +
                                "send https requests through 'https' protocol.",
                        defaultValue = "<empty_string>",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "client.truststore.password",
                        description = " The password for client-truststore to send https requests. A custom password " +
                                "can be specified if required. ",
                        defaultValue = "<empty_string>",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "headers",
                        description = "Headers that should be included as HTTP request headers in the request. " +
                                "\nThe format of the supported input is as follows, \n" +
                                "\"\'header1:value1\',\'header2:value2\'\"",
                        defaultValue = "<empty_string>",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "job",
                        description = " This property defines the job name of the exported Prometheus metrics " +
                                "that has to be fetched.",
                        defaultValue = "<empty_string>",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "instance",
                        description = "This property defines the instance of the exported Prometheus metrics " +
                                "that has to be fetched.",
                        defaultValue = "<empty_string>",
                        optional = true,
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "grouping.key",
                        description = "This parameter specifies the grouping key of the required metrics in " +
                                "key-value pairs. Grouping key is used if the metrics are exported by Prometheus" +
                                " pushGateway in order to distinguish the metrics from already existing metrics.\n " +
                                "The expected format of the grouping key is as follows: \n" +
                                "\"\'key1:value1\',\'key2:value2\'\"",
                        defaultValue = "<empty_string>",
                        optional = true,
                        type = {DataType.STRING}
                ),
        },
        examples = {
                @Example(
                        syntax = "@source(type= 'prometheus', target.url= 'http://localhost:9080/metrics', " +
                                "metric.type= 'counter', metric.name= 'sweet_production_counter', @map(type= " +
                                "‘keyvalue’))\n" +
                                "define stream FooStream1(metric_name string, metric_type string, help string, " +
                                "subtype string, name string, quantity string, value double);\n",
                        description = "In this example, the prometheus source makes an http request to the " +
                                "\'target.url\' and analyse the response. From the analysed response, the source " +
                                "retrieves the Prometheus counter metrics with the name, 'sweet_production_counter' " +
                                "and converts the filtered metrics into Siddhi events using the key-value mapper." +
                                "\nThe generated maps will have keys and values as follows: \n" +
                                "  metric_name  -> sweet_production_counter\n" +
                                "  metric_type  -> counter\n" +
                                "  help  -> <help_string_of_metric>\n" +
                                "  subtype  -> null\n" +
                                "  name -> <value_of_label_name>\n" +
                                "  quantity -> <value_of_label_quantity>\n" +
                                "  value -> <value_of_metric>\n"
                ),
                @Example(
                        syntax = "@source(type= 'prometheus', target.url= 'http://localhost:9080/metrics', " +
                                "metric.type= 'summary', metric.name= 'sweet_production_summary', @map(type= " +
                                "‘keyvalue’))\n define stream FooStream2(metric_name string, metric_type string, help" +
                                " string, subtype string, name string, quantity string, quantile string, value " +
                                "double);\n",
                        description = "In this example, the prometheus source makes an http request to the " +
                                "\'target.url\' and analyses the response. From the analysed response, the source " +
                                "retrieves the Prometheus summary metrics with the name, 'sweet_production_summary' " +
                                "and converts the filtered metrics into Siddhi events using the key-value mapper." +
                                "\nThe generated maps have keys and values as follows: \n" +
                                "  metric_name  -> sweet_production_summary\n" +
                                "  metric_type  -> summary\n" +
                                "  help  -> <help_string_of_metric>\n" +
                                "  subtype  -> <'sum'/'count'/'null'>\n" +
                                "  name -> <value_of_label_name>\n" +
                                "  quantity -> <value_of_label_quantity>\n" +
                                "  quantile  -> <value of the quantile>\n" +
                                "  value -> <value_of_metric>\n"
                ),
                @Example(
                        syntax = "@source(type= 'prometheus', target.url= 'http://localhost:9080/metrics', " +
                                "metric.type= 'histogram', metric.name= 'sweet_production_histogram', @map(type= " +
                                "‘keyvalue’))\n" +
                                "define stream FooStream3(metric_name string, metric_type string, help string, " +
                                "subtype string, name string, quantity string, le string, value double);\n",
                        description = "In this example, the prometheus source will make an http request to the " +
                                "\'target.url\' and analyse the response. From the analysed response, the source " +
                                "retrieves the Prometheus histogram metrics with name 'sweet_production_histogram' " +
                                "and converts the filtered metrics into Siddhi events using the key-value mapper." +
                                "\nThe generated maps will have keys and values as follows, \n" +
                                "  metric_name  -> sweet_production_histogram\n" +
                                "  metric_type  -> histogram\n" +
                                "  help  -> <help_string_of_metric>\n" +
                                "  subtype  -> <'sum'/'count'/'bucket'>\n" +
                                "  name -> <value_of_label_name>\n" +
                                "  quantity -> <value_of_label_quantity>\n" +
                                "  le  -> <value of the bucket>\n" +
                                "  value -> <value_of_metric>\n"
                )
        },
        systemParameter = {
                @SystemParameter(
                        name = "scrapeInterval",
                        description = "The default time interval in seconds for the Prometheus source to make HTTP " +
                                "requests to the target URL.",
                        defaultValue = "60",
                        possibleParameters = "Any integer value"
                ),
                @SystemParameter(
                        name = "scrapeTimeout",
                        description = "This default time duration (in seconds) for an HTTP request to time-out if the" +
                                " server at the URL does not respond. ",
                        defaultValue = "10",
                        possibleParameters = "Any integer value"
                ),
                @SystemParameter(
                        name = "scheme",
                        description = "The scheme of the target for Prometheus source to make HTTP requests." +
                                " The supported schemes are HTTP and HTTPS.",
                        defaultValue = "HTTP",
                        possibleParameters = "HTTP or HTTPS"
                ),
                @SystemParameter(
                        name = "username",
                        description = "The username that has to be added in the authorization header of the HTTP " +
                                "request, if basic authentication is enabled at the target. It is required to " +
                                "specify both username and password to enable basic authentication. If one of " +
                                "the parameter is not given by user then an error is logged in the console.",
                        defaultValue = "<empty_string>",
                        possibleParameters = "Any string"
                ),
                @SystemParameter(
                        name = "password",
                        description = "The password that has to be added in the authorization header of the HTTP " +
                                "request, if basic authentication is enabled at the target. It is required to " +
                                "specify both username and password to enable basic authentication. If one of" +
                                " the parameter is not given by user then an error is logged in the console.",
                        defaultValue = "<empty_string>",
                        possibleParameters = "Any string"
                ),
                @SystemParameter(
                        name = "trustStoreFile",
                        description = "The default file path to the location of truststore that the client needs " +
                                "to send for HTTPS requests through 'HTTPS' protocol.",
                        defaultValue = "${carbon.home}/resources/security/client-truststore.jks",
                        possibleParameters = "Any valid path for the truststore file"
                ),
                @SystemParameter(
                        name = "trustStorePassword",
                        description = "The default password for the client-truststore to send HTTPS requests.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "Any string"
                ),
                @SystemParameter(
                        name = "headers",
                        description = "The headers that should be included as HTTP request headers in the scrape " +
                                "request. \nThe format of the supported input is as follows, \n" +
                                "\"\'header1:value1\',\'header2:value2\'\"",
                        defaultValue = "<empty_string>",
                        possibleParameters = "Any valid http headers"
                ),
                @SystemParameter(
                        name = "job",
                        description = " The default job name of the exported Prometheus metrics " +
                                "that has to be fetched.",
                        defaultValue = "<empty_string>",
                        possibleParameters = "Any valid job name"
                ),
                @SystemParameter(
                        name = "instance",
                        description = "The default instance of the exported Prometheus metrics " +
                                "that has to be fetched.",
                        defaultValue = "<empty_string>",
                        possibleParameters = "Any valid instance name"
                ),
                @SystemParameter(
                        name = "groupingKey",
                        description = "The default grouping key of the required Prometheus metrics in key-value " +
                                "pairs. Grouping key is used if the metrics are exported by Prometheus pushGateway " +
                                "in order to distinguish the metrics from already existing metrics. " +
                                "\nThe expected format of the grouping key is as follows: \n" +
                                "\"\'key1:value1\',\'key2:value2\'\"",
                        defaultValue = "<empty_string>",
                        possibleParameters = "Any valid grouping key pairs"
                )
        }
)

public class PrometheusSource extends Source {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private String targetURL;
    private String streamName;
    private String scheme;
    private long scrapeIntervalInSeconds;

    private PrometheusScraper prometheusScraper;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] requestedTransportPropertyNames, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        streamName = sourceEventListener.getStreamDefinition().getId();
        initPrometheusScraper(optionHolder, configReader, sourceEventListener, siddhiAppContext);
        configureMetricAnalyser(optionHolder, configReader, siddhiAppContext);
        prometheusScraper.createConnectionChannel();
    }

    private void initPrometheusScraper(OptionHolder optionHolder, ConfigReader configReader,
                                       SourceEventListener sourceEventListener, SiddhiAppContext siddhiAppContext) {

        this.targetURL = optionHolder.validateAndGetStaticValue(PrometheusConstants.TARGET_URL,
                configReader.readConfig(PrometheusConstants.TARGET_URL_CONFIGURATION, EMPTY_STRING));
        this.scheme = optionHolder.validateAndGetStaticValue(PrometheusConstants.SCHEME, configReader
                .readConfig(PrometheusConstants.SCHEME_CONFIGURATION, PrometheusConstants.HTTP_SCHEME));
        if (!(scheme.equalsIgnoreCase(PrometheusConstants.HTTP_SCHEME) || scheme.equalsIgnoreCase(
                PrometheusConstants.HTTPS_SCHEME))) {
            throw new SiddhiAppCreationException("The field \'scheme\' contains unsupported value \'" + scheme + "\' " +
                    "in " + streamName + " of " + PrometheusConstants.PROMETHEUS_SOURCE);
        }
        if (PrometheusSourceUtil.checkEmptyString(targetURL)) {
            throw new SiddhiAppCreationException("The target URL field found empty but it is a Mandatory field of " +
                    "" + PrometheusConstants.PROMETHEUS_SOURCE + " in " + streamName);
        }
        try {
            URL url = new URL(targetURL);
            if (!(url.getProtocol()).equalsIgnoreCase(scheme)) {
                throw new SiddhiAppCreationException("The provided scheme and the scheme of target URL are " +
                        "not matching in Prometheus source associated with stream " + streamName);
            }
        } catch (MalformedURLException e) {
            throw new SiddhiAppCreationException("The Prometheus source associated with stream " + streamName +
                    " contains an invalid value \'" + targetURL + "\' for target URL" , e);
        }
        scrapeIntervalInSeconds = validateAndSetNumericValue(optionHolder.validateAndGetStaticValue(
                PrometheusConstants.SCRAPE_INTERVAL, configReader.readConfig(
                        PrometheusConstants.SCRAPE_INTERVAL_CONFIGURATION,
                        PrometheusConstants.DEFAULT_SCRAPE_INTERVAL)), PrometheusConstants.SCRAPE_INTERVAL);
        long scrapeTimeoutInSeconds = validateAndSetNumericValue(optionHolder.validateAndGetStaticValue(
                PrometheusConstants.SCRAPE_TIMEOUT,
                configReader.readConfig(PrometheusConstants.SCRAPE_TIMEOUT_CONFIGURATION,
                        PrometheusConstants.DEFAULT_SCRAPE_TIMEOUT)), PrometheusConstants.SCRAPE_TIMEOUT);
        String userName = optionHolder.validateAndGetStaticValue(PrometheusConstants.USERNAME_BASIC_AUTH,
                configReader.readConfig(PrometheusConstants.USERNAME_BASIC_AUTH_CONFIGURATION, EMPTY_STRING));
        String password = optionHolder.validateAndGetStaticValue(PrometheusConstants.PASSWORD_BASIC_AUTH,
                configReader.readConfig(PrometheusConstants.PASSWORD_BASIC_AUTH_CONFIGURATION, EMPTY_STRING));
        String clientStoreFile = optionHolder.validateAndGetStaticValue(PrometheusConstants.TRUSTSTORE_FILE,
                PrometheusSourceUtil.trustStorePath(configReader));
        String clientStorePassword = optionHolder.validateAndGetStaticValue(PrometheusConstants.TRUSTSTORE_PASSWORD,
                PrometheusSourceUtil.trustStorePassword(configReader));
        String headers = optionHolder.validateAndGetStaticValue(PrometheusConstants.REQUEST_HEADERS,
                configReader.readConfig(PrometheusConstants.REQUEST_HEADERS_CONFIGURATION, EMPTY_STRING));

        List<Header> headerList = PrometheusSourceUtil.getHeaders(headers, streamName);
        this.prometheusScraper = new PrometheusScraper(targetURL, scheme, scrapeTimeoutInSeconds, headerList,
                sourceEventListener, streamName);
        if ((!PrometheusSourceUtil.checkEmptyString(userName) || !PrometheusSourceUtil.checkEmptyString(password))) {
            if ((PrometheusSourceUtil.checkEmptyString(userName) || PrometheusSourceUtil.checkEmptyString(password))) {
                throw new SiddhiAppCreationException("Please provide user name and password in " +
                        PrometheusConstants.PROMETHEUS_SOURCE + " associated with the stream " + streamName + " in " +
                        "Siddhi app " + siddhiAppContext.getName());
            }
            prometheusScraper.setAuthorizationCredentials(userName, password);
        }

        if (PrometheusConstants.HTTPS_SCHEME.equalsIgnoreCase(scheme) &&
                ((PrometheusSourceUtil.checkEmptyString(clientStoreFile)) ||
                (PrometheusSourceUtil.checkEmptyString(clientStorePassword)))) {

            throw new SiddhiAppCreationException("Client trustStore file path or password are empty while " +
                    "default scheme is 'https'. Please provide client " +
                    "trustStore file path and password in " + streamName + " of " +
                    PrometheusConstants.PROMETHEUS_SOURCE);
        }
        if (PrometheusConstants.HTTPS_SCHEME.equalsIgnoreCase(scheme)) {
            prometheusScraper.setHttpsProperties(clientStoreFile, clientStorePassword);
        }
    }

    private long validateAndSetNumericValue(String value, String field) {
        long number;
        try {
            number = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new SiddhiAppCreationException("Invalid value \'" + value + "\' is found inside the field" +
                    " \'" + field + "\' from " + PrometheusConstants.PROMETHEUS_SOURCE + " associated with stream \'" +
                    "" + streamName + "\'. Please provide a valid numeric value.", e);
        }
        if (number < 0) {
            throw new SiddhiAppCreationException("The value \'" + value + "\' of field \'" + field + "\' from " +
                    PrometheusConstants.PROMETHEUS_SOURCE + " cannot be negative in " + streamName);
        }
        return number;
    }

    private void configureMetricAnalyser(OptionHolder optionHolder, ConfigReader configReader,
                                         SiddhiAppContext siddhiAppContext) {
        String metricName = optionHolder.validateAndGetStaticValue(PrometheusConstants.METRIC_NAME, streamName);
        MetricType metricType = MetricType.assignMetricType(optionHolder.
                        validateAndGetStaticValue(PrometheusConstants.METRIC_TYPE),
                streamName);
        String job = optionHolder.validateAndGetStaticValue(PrometheusConstants.METRIC_JOB,
                configReader.readConfig(PrometheusConstants.METRIC_JOB_CONFIGURATION, EMPTY_STRING));
        String instance = optionHolder.validateAndGetStaticValue(PrometheusConstants.METRIC_INSTANCE,
                configReader.readConfig(PrometheusConstants.METRIC_INSTANCE_CONFIGURATION, EMPTY_STRING));
        Map<String, String> groupingKeyMap = PrometheusSourceUtil.populateStringMap(
                optionHolder.validateAndGetStaticValue(PrometheusConstants.METRIC_GROUPING_KEY, EMPTY_STRING),
                streamName);
        Attribute.Type valueType;
        try {
            valueType = getStreamDefinition().getAttributeType(PrometheusConstants.VALUE_STRING);
            if (valueType.equals(Attribute.Type.STRING) || valueType.equals(Attribute.Type.BOOL) ||
                    valueType.equals(Attribute.Type.OBJECT)) {
                throw new SiddhiAppCreationException("The attribute \'" + PrometheusConstants.VALUE_STRING + "\' " +
                        "contains unsupported type \'" + valueType.toString() + "\' in " +
                        PrometheusConstants.PROMETHEUS_SOURCE + " associated with stream \'" + streamName + "\'");
            }
        } catch (AttributeNotExistException e) {
            throw new SiddhiAppCreationException("The value attribute \'" + PrometheusConstants.VALUE_STRING + "\' is" +
                    " not found in " + PrometheusConstants.PROMETHEUS_SOURCE + " associated with stream \'"
                    + streamName + "\'", e);
        }

        prometheusScraper.setMetricProperties(metricName, metricType, job, instance, groupingKeyMap, valueType);
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{Map.class};
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        PrometheusScraper.CompletionCallback completionCallback = (Throwable error) ->
        {
            if (error.getClass().equals(ConnectionUnavailableException.class)) {
                connectionCallback.onError(new ConnectionUnavailableException(
                        "Connection to the target is lost.", error));
            } else {
                destroy();
                throw new SiddhiAppRuntimeException("Failed while retrieving and analysing the metrics.", error);
            }
        };
        prometheusScraper.setCompletionCallback(completionCallback);
        executorService.scheduleWithFixedDelay(prometheusScraper, 0, scrapeIntervalInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void disconnect() {
        executorService.shutdown();
        prometheusScraper.pause();
        prometheusScraper.clearConnectorFactory();
    }

    @Override
    public void destroy() {
        prometheusScraper.clearPrometheusScraper();
        prometheusScraper.clearConnectorFactory();
    }

    @Override
    public void pause() {
        prometheusScraper.pause();
    }

    @Override
    public void resume() {
        prometheusScraper.resume();
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> currentState = new HashMap<>();
        currentState.put(PrometheusConstants.LAST_RETRIEVED_SAMPLES, prometheusScraper.getLastValidResponse());
        return currentState;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
        prometheusScraper.setLastValidResponse((List<String>) map.get(PrometheusConstants.LAST_RETRIEVED_SAMPLES));

    }

}

