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

package org.wso2.extension.siddhi.io.prometheus.util;

/**
 * {@code PrometheusConstants} Prometheus Sink SinkConstants.
 */
public class PrometheusConstants {

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

    public static final String DEFAULT_JOB_NAME = "siddhiJob";
    public static final String DEFAULT_PUBLISH_MODE = "server";
    public static final String DEFAULT_PUSH_URL = "http://localhost:9091";
    public static final String DEFAULT_SERVER_URL = "http://localhost:9080";
    public static final String DEFAULT_ERROR = "0.001";
    public static final String VALUE_STRING = "value";
    public static final String METRIC_NAME_REGEX = "[a-zA-Z_:][a-zA-Z0-9_:]*";

    public static final String SERVER_PUBLISH_MODE = "server";
    public static final String PUSHGATEWAY_PUBLISH_MODE = "pushgateway";

    public static final String PUSH_OPERATION = "push";
    public static final String PUSH_ADD_OPERATION = "pushadd";

    public static final String KEY_VALUE_SEPARATOR = "','";
    public static final String VALUE_SEPARATOR = ":";
    public static final String ELEMENT_SEPARATOR = "\\s*,\\s*";

    public static final String REGISTERED_METRICS = "Last registered sample";
    public static final String MAP_ANNOTATION = "map";
    public static final String PAYLOAD_ANNOTATION = "payload";


    public static final String JOB_NAME_CONFIGURATION = "jobName";
    public static final String PUSH_URL_CONFIGURATION = "pushURL";
    public static final String SERVER_URL_CONFIGURATION = "serverURL";
    public static final String PUBLISH_MODE_CONFIGURATION = "publishMode";
    public static final String GROUPING_KEY_CONFIGURATION = "groupingKey";

}
