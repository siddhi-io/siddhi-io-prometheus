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

import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;

/**
 * Test cases for invalid sink definitions.
 */
public class SinkValidationTestcase {
    private static final Logger log = Logger.getLogger(SinkValidationTestcase.class);
    private static String pushgatewayURL;
    private static String serverURL;
    private static String buckets;
    private static String quantiles;
    private static final String ERROR_MESSAGE = "Error on \'(.*)\' @ Line: (.*). Position: (.*), near \'(.*)\'. ";
    private static final String REGEX_PATTERN = "[a-zA-Z_:][a-zA-Z0-9_:]*";

    @BeforeClass
    public static void startTest() {
        log.info("== Prometheus sink validation tests started ==");
        pushgatewayURL = "http://localhost:9095";
        serverURL = "http://localhost:9096";
        buckets = "2, 4, 6, 8";
        quantiles = "0.4,0.65,0.85";
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        Thread.sleep(100);
        log.info("== Prometheus sink validation tests completed ==");
    }


    private void startSiddhiApp(String streamDefinition) {
        SiddhiManager siddhiManager = new SiddhiManager();
        String query = (
                "@info(name = 'query') "
                        + "from InputStream "
                        + "select symbol, value "
                        + "insert into SinkTestStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streamDefinition + query);
        siddhiAppRuntime.start();
    }


    /**
     * test for Prometheus sink with invalid input parameters and values.
     *
     * @throws Exception Interrupted exception
     */
    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE +
                    "Custom mapping associated with stream \'(.*)\' is not supported by Prometheus sink")
    public void prometheusValidationTest1() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test with custom mapping");
        log.info("----------------------------------------------------------------------------------");
        String streamDefinition1 =
                "define stream InputStream (symbol String, value int, price double);" +
                        "@sink(type='prometheus',job='sinkTest'," +
                        "publish.mode='pushgateway', " +
                        "metric.type='gauge', " +
                        "metric.help= 'Metric definition test', " +
                        "@map(type = \'keyvalue\', @payload(mode = 'mode', value = 'value')))" +
                        "Define stream SinkTestStream (symbol String, value int);";
        startSiddhiApp(streamDefinition1);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "Invalid publish mode : (.*) in Prometheus sink " +
                    "associated with stream \'(.*)\'.")
    public void prometheusValidationTest2() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test with undefined publish mode");
        log.info("----------------------------------------------------------------------------------");

        String publishMode = "service";

        String streamDefinition2 = "" +
                "define stream InputStream (symbol String, value int, price double);" +
                "@sink(type='prometheus',job='sinkTest'," +
                "publish.mode='" + publishMode + "', metric.type='histogram', " +
                "metric.help= 'Metric type definition test', " +
                "@map(type = \'keyvalue\'))" +
                "Define stream SinkTestStream (symbol String, value int, price double);";
        startSiddhiApp(streamDefinition2);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "The \'metric.type\' field in Prometheus sink " +
                    "associated with stream \'(.*)\' contains illegal value")
    public void prometheusValidationTest3() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test with undefined metric type");
        log.info("----------------------------------------------------------------------------------");

        String metricType = "typeMetric";
        String streamDefinition3 = "" +
                "define stream InputStream (symbol String, value int, price double);" +
                "@sink(type='prometheus',job='sinkTest'," +
                "publish.mode='pushgateway', metric.type='" + metricType + "', " +
                "metric.help= 'Metric type definition test', " +
                "@map(type = \'keyvalue\'))" +
                "Define stream SinkTestStream (symbol String, value int, price double);";
        startSiddhiApp(streamDefinition3);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "The buckets field in Prometheus sink associated with " +
                    "stream \'(.*)\' is not supported for metric type \'(.*)\'.")
    public void prometheusValidationTest4() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test with unsupported metric type with buckets");
        log.info("----------------------------------------------------------------------------------");

        String streamDefinition4 = "" +
                "define stream InputStream (symbol String, value int, price double);" +
                "@sink(type='prometheus',job='sinkTest'," +
                "server.url='" + serverURL + "', publish.mode='server', metric.type='counter', " +
                "metric.help= 'Metric type definition test', buckets= '" + buckets + "'," +
                "@map(type = \'keyvalue\'))" +
                "Define stream SinkTestStream (symbol String, value int, price double);";

        startSiddhiApp(streamDefinition4);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "The buckets/quantiles field in Prometheus sink " +
                    "associated with the stream \'(.*)\' is not in the expected format. " +
                    "please insert the numerical values as \"2,3,4,5\".")
    public void prometheusValidationTest5() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test with unsupported values for buckets");
        log.info("----------------------------------------------------------------------------------");
        String streamDefinition5 = "" +
                "define stream InputStream (symbol String, value int, price double);"
                + "@sink(type='prometheus',job='sinkTest'," +
                "push.url='" + pushgatewayURL + "', publish.mode='pushgateway', metric.type='histogram', " +
                "metric.help= 'Metric type definition test', buckets= '2,a,b,3'," +
                "@map(type = \'keyvalue\'))"
                + "Define stream MetricTypeTestStream (symbol String, value int, price double);";
        startSiddhiApp(streamDefinition5);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "The quantiles field in Prometheus sink associated " +
                    "with stream \'(.*)\' is not supported for metric type \'(.*)\'.")
    public void prometheusValidationTest6() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test with unsupported metric type for quantiles");
        log.info("----------------------------------------------------------------------------------");

        String streamDefinition6 = "" +
                "define stream InputStream (symbol String, value int, price double);" +
                "@sink(type='prometheus',job='sinkTest'," +
                "server.url='" + serverURL + "', publish.mode='server', metric.type='histogram', " +
                "metric.help= 'Metric type definition test', quantiles= '" + quantiles + "'," +
                "@map(type = \'keyvalue\'))" +
                "Define stream MetricTypeTestStream (symbol String, value int, price double);";
        startSiddhiApp(streamDefinition6);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "The values assigned for quantiles in Prometheus " +
                    "sink associated with stream \'(.*)\' are invalid." +
                    "Please insert values between 0 and 1.")
    public void prometheusValidationTest7() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test with unsupported values for quantiles");
        log.info("----------------------------------------------------------------------------------");

        String streamDefinition7 = "" +
                "define stream InputStream (symbol String, value int, price double);"
                + "@sink(type='prometheus',job='sinkTest'," +
                "push.url='" + pushgatewayURL + "', publish.mode='pushgateway', metric.type='summary', " +
                "metric.help= 'Metric type definition test', quantiles= '0.2,5,2,0.86'," +
                "@map(type = \'keyvalue\'))"
                + "Define stream MetricTypeTestStream (symbol String, value int, price double);";
        startSiddhiApp(streamDefinition7);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "Metric name \'(.*)\' does not match the regex " +
                    "\"(.*)\" in Prometheus sink associated with stream \'(.*)\'.")
    public void prometheusValidationTest8() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test with user defined metric name in unsupported format");
        log.info("----------------------------------------------------------------------------------");

        String metricName = "metric name test";
        String streamDefinition8 = "" +
                "define stream InputStream (symbol String, value int, price double);" +
                "@sink(type='prometheus',job='prometheusSinkTest'," +
                "push.url='" + pushgatewayURL + "', publish.mode='pushgateway', metric.type='summary', " +
                "metric.help= 'Summary definition test', metric.name= '" + metricName + "', " +
                "quantiles = '" + quantiles + "',@map(type = 'keyvalue'))" +
                "Define stream SummaryTestStream (symbol String, value int, price double);";
        startSiddhiApp(streamDefinition8);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "The value attribute (.*) is not found " +
                    "in Prometheus sink associated with stream \'(.*)\'")
    public void prometheusValidationTest9() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test without value attribute configuration or" +
                " 'value' attribute in stream definition");
        log.info("----------------------------------------------------------------------------------");

        String valueAttribute = "'value'";
        String streamDefinition9 = "" +
                "define stream InputStream (symbol String, volume int, price double);" +
                "@sink(type='prometheus',job='prometheusSinkTest'," +
                "push.url='" + pushgatewayURL + "', publish.mode='pushgateway', metric.type='summary', " +
                "metric.help= 'Summary definition test', metric.name= 'metric_name_test_value', " +
                "quantiles = '" + quantiles + "',@map(type = 'keyvalue'))" +
                "Define stream SummaryTestStream (symbol String, volume int, price double);";
        startSiddhiApp(streamDefinition9);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "The field value attribute \'(.*)\'contains " +
                    "unsupported type in Prometheus sink associated with stream \'(.*)\'")
    public void prometheusValidationTest10() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test without value attribute in unsupported type");
        log.info("----------------------------------------------------------------------------------");
        String streamDefinition10 = "" +
                "define stream InputStream (symbol String, value string, price double);" +
                "@sink(type='prometheus',job='prometheusSinkTest'," +
                "server.url='" + serverURL + "', publish.mode='server', metric.type='summary', " +
                "metric.help= 'Summary definition test', metric.name= 'metric_name_test_summary', " +
                "quantiles = '" + quantiles + "', @map(type = 'keyvalue'))" +
                "Define stream SummaryTestStream (symbol String, value string, price double);";
        startSiddhiApp(streamDefinition10);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "Invalid value for push operation : (.*) in Prometheus" +
                    " sink associated with stream \'(.*)\'.")
    public void prometheusValidationTest11() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test with invalid value for push operation");
        log.info("----------------------------------------------------------------------------------");

        String pushOperation = "pushMetric";
        String streamDefinition11 = "" +
                "define stream InputStream (symbol String, volume int, price double);" +
                "@sink(type='prometheus',job='prometheusSinkTest'," +
                "push.url='" + pushgatewayURL + "', publish.mode='pushgateway', metric.type='summary', " +
                "metric.help= 'Summary definition test', metric.name= 'metric_name_test_value', " +
                "quantiles = '" + quantiles + "', " +
                "push.operation = '" + pushOperation + "',@map(type = 'keyvalue'))" +
                "Define stream SummaryTestStream (symbol String, value int, price double);";
        startSiddhiApp(streamDefinition11);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class,
            expectedExceptionsMessageRegExp = ERROR_MESSAGE + "The grouping key field in Prometheus sink associated " +
                    "with the stream \'(.*)\' is not in the expected format. " +
                    "please insert them as 'key1:val1','key2:val2'.")
    public void prometheusValidationTest12() throws InterruptedException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Sink test with grouping key configuration in unsupported format");
        log.info("----------------------------------------------------------------------------------");

        String groupingKey = "key1-value1,key2-value2";
        String streamDefinition12 = "" +
                "define stream InputStream (symbol String, volume int, price double);" +
                "@sink(type='prometheus',job='prometheusSinkTest'," +
                "push.url='" + pushgatewayURL + "', publish.mode='pushgateway', metric.type='counter', " +
                "metric.help= 'Counter definition test'," +
                "grouping.key = '" + groupingKey + "',@map(type = 'keyvalue'))" +
                "Define stream SummaryTestStream (symbol String, value int, price double);";
        startSiddhiApp(streamDefinition12);
    }
}

