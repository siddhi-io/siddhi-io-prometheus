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

/**
 * {@code PrometheusConstants} Prometheus Sink SinkConstants.
 */
public class PrometheusConstants {

    //Constants for Prometheus-sink
    public static final String JOB_NAME = "job";
    public static final String PUSH_URL = "push.url";
    public static final String SERVER_URL = "server.url";
    public static final String METRIC_PUBLISH_MODE = "publish.mode";
    public static final String METRIC_TYPE = "metric.type";
    public static final String METRIC_HELP = "metric.help";
    public static final String METRIC_NAME = "metric.name";
    public static final String BUCKET_DEFINITION = "buckets";
    public static final String QUANTILES_DEFINITION = "quantiles";
    public static final String VALUE_ATTRIBUTE = "value.attribute";
    public static final String PUSH_DEFINITION = "push.operation";
    public static final String GROUPING_KEY_DEFINITION = "grouping.key";
    public static final String QUANTILE_ERROR = "quantile.error";

    public static final String EMPTY_STRING = "";
    public static final String HELP_STRING = "help for ";
    public static final String SPACE_STRING = " ";

    //Default values for Prometheus-sink
    static final String DEFAULT_JOB_NAME = "siddhiJob";
    static final String DEFAULT_PUBLISH_MODE = "server";
    static final String DEFAULT_PUSH_URL = "http://localhost:9091";
    static final String DEFAULT_SERVER_URL = "http://localhost:9080";
    public static final String DEFAULT_ERROR = "0.001";
    public static final String VALUE_STRING = "value";
    public static final String METRIC_NAME_REGEX = "[a-zA-Z_:][a-zA-Z0-9_:]*";

    public static final String SERVER_PUBLISH_MODE = "server";
    public static final String PUSHGATEWAY_PUBLISH_MODE = "pushgateway";

    public static final String PUSH_OPERATION = "push";
    public static final String PUSH_ADD_OPERATION = "pushadd";

    static final String KEY_VALUE_SEPARATOR = "','";
    static final String VALUE_SEPARATOR = ":";
    static final String ELEMENT_SEPARATOR = "\\s*,\\s*";

    public static final String REGISTERED_METRICS = "Last registered sample";
    public static final String MAP_ANNOTATION = "map";
    public static final String PAYLOAD_ANNOTATION = "payload";

    //System parameter names for Prometheus-sink
    static final String JOB_NAME_CONFIGURATION = "jobName";
    static final String PUSH_URL_CONFIGURATION = "pushURL";
    static final String SERVER_URL_CONFIGURATION = "serverURL";
    static final String PUBLISH_MODE_CONFIGURATION = "publishMode";
    static final String GROUPING_KEY_CONFIGURATION = "groupingKey";

    //Constants for Prometheus-source
    public static final String TARGET_URL = "target.url";
    public static final String SCRAPE_INTERVAL = "scrape.interval";
    public static final String SCRAPE_TIMEOUT = "scrape.timeout";
    public static final String SCHEME = "scheme";
    public static final String USERNAME_BASIC_AUTH = "username";
    public static final String PASSWORD_BASIC_AUTH = "password";
    public static final String TRUSTSTORE_FILE = "client.truststore.file";
    public static final String TRUSTSTORE_PASSWORD = "client.truststore.password";
    public static final String REQUEST_HEADERS = "headers";
    public static final String METRIC_JOB = "job";
    public static final String METRIC_INSTANCE = "instance";
    public static final String METRIC_GROUPING_KEY = "grouping.key";

    //System parameter names for Prometheus-source
    public static final String TARGET_URL_CONFIGURATION = "targetURL";
    public static final String SCRAPE_INTERVAL_CONFIGURATION = "scrapeInterval";
    public static final String SCRAPE_TIMEOUT_CONFIGURATION = "scrapeTimeout";
    public static final String SCHEME_CONFIGURATION = "scheme";
    public static final String USERNAME_BASIC_AUTH_CONFIGURATION = "username";
    public static final String PASSWORD_BASIC_AUTH_CONFIGURATION = "password";
    static final String TRUSTSTORE_FILE_CONFIGURATION = "trustStoreFile";
    static final String TRUSTSTORE_PASSWORD_CONFIGURATION = "trustStorePassword";
    public static final String REQUEST_HEADERS_CONFIGURATION = "headers";
    public static final String METRIC_JOB_CONFIGURATION = "job";
    public static final String METRIC_INSTANCE_CONFIGURATION = "instance";

    public static final String HTTP_SCHEME = "http";
    public static final String HTTPS_SCHEME = "https";

    //Default values for Prometheus-source
    public static final String DEFAULT_SCRAPE_INTERVAL = "60";
    public static final String DEFAULT_SCRAPE_TIMEOUT = "10";
    public static final String PROMETHEUS_SOURCE = "Prometheus source";
    static final int DEFAULT_HTTPS_PORT = 443;
    public static final String DEFAULT_HTTP_METHOD = "GET";
    static final int DEFAULT_HTTP_PORT = 80;
    static final String TRUSTSTORE_PATH_VALUE = "${carbon.home}/resources/security/client-truststore.jks";
    static final String TRUSTSTORE_PASSWORD_VALUE = "wso2carbon";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_METHOD = "HTTP_METHOD";

    //Map keys and values for Prometheus-source
    public static final String MAP_NAME = "metric_name";
    public static final String MAP_TYPE = "metric_type";
    public static final String MAP_HELP = "help";
    public static final String MAP_SAMPLE_SUBTYPE = "subtype";
    public static final String MAP_SAMPLE_VALUE = "value";
    public static final String SUBTYPE_NULL = "null";
    public static final String SUBTYPE_BUCKET = "bucket";
    public static final String SUBTYPE_COUNT = "count";
    public static final String SUBTYPE_SUM = "sum";

    public static final String BUCKET_POSTFIX = "_bucket";
    public static final String COUNT_POSTFIX = "_count";
    public static final String SUM_POSTFIX = "_sum";
    public static final String LAST_RETRIEVED_SAMPLES = "last.retrieved.samples";
    public static final String LE_KEY = "le";
    public static final String QUANTILE_KEY = "quantile";
}
