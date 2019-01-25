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

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.HTTPServer;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.CannotRestoreSiddhiAppStateException;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test cases for the functionality of prometheus source
 */
public class PrometheusSourceTest {

    private static final Logger log = Logger.getLogger(PrometheusSourceTest.class);
    private String targetURL;
    private HTTPServer server;
    private String serverPort;
    private AtomicInteger eventCount = new AtomicInteger(0);
    private AtomicBoolean eventArrived = new AtomicBoolean(false);
    private Map<String, ArrayList<Object[]>> eventMap = new HashMap<>();
    private List<Object[]> receivedEvents = new ArrayList<>();

    @BeforeClass
    public void startTest() {
        serverPort = System.getenv("SERVER_PORT");
        String host = System.getenv("HOST_NAME");
        log.info("== Prometheus source tests started ==");
        targetURL = "http://" + host + ":" + serverPort + "/metrics";
    }

    private void initializeMetrics(int port) {
        CollectorRegistry registry = new CollectorRegistry();
        buildMetrics(registry);
        try {
            server = new HTTPServer(new InetSocketAddress(port), registry);
        } catch (IOException e) {
            log.error("Unable to establish connection at server : " + e);
        }
    }

    private void buildMetrics(CollectorRegistry registry) {
        Counter counter = Counter.build()
                .name("counter_test")
                .help("unit test - for counter metric")
                .labelNames("symbol", "price")
                .register(registry);
        counter.labels("WSO2", "78.8").inc(100);
        counter.labels("IBM", "65.32").inc(125);

        Gauge gauge = Gauge.build()
                .name("gauge_test")
                .help("unit test - for gauge metric")
                .labelNames("symbol", "price")
                .register(registry);
        gauge.labels("WSO2", "78.8").inc(100);
        gauge.labels("IBM", "65.32").inc(125);

        Histogram histogram = Histogram.build()
                .name("histogram_test")
                .help("unit test - for histogram metric")
                .labelNames("symbol", "price")
                .buckets(50, 70, 90)
                .register(registry);
        histogram.labels("WSO2", "78.8").observe(100);
        histogram.labels("IBM", "65.32").observe(125);


        Summary summary = Summary.build()
                .name("summary_test")
                .help("unit test - for summary metric")
                .labelNames("symbol", "price")
                .quantile(0.25, 0.001)
                .quantile(0.5, 0.001)
                .quantile(0.75, 0.001)
                .quantile(1, 0.0001)
                .register(registry);
        summary.labels("WSO2", "78.8").observe(100);
        summary.labels("IBM", "65.32").observe(125);

    }

    private List<Object[]> retrieveEventList(String metricType) {
        List<Object[]> eventList = new ArrayList<>();
        switch (metricType.toUpperCase(Locale.ENGLISH)) {
            case "COUNTER": {
                eventList.add(new Object[]{"counter_test", "counter", "unit test - for counter metric", "WSO2",
                        "78.8", "null", 100});
                eventList.add(new Object[]{"counter_test", "counter", "unit test - for counter metric", "IBM",
                        "65.32", "null", 125});
                break;
            }
            case "GAUGE": {
                eventList.add(new Object[]{"gauge_test", "gauge", "unit test - for gauge metric", "WSO2",
                        "78.8", "null", 100.0});
                eventList.add(new Object[]{"gauge_test", "gauge", "unit test - for gauge metric", "IBM",
                        "65.32", "null", 125.0});
                break;
            }
            default:
                //default execution is not needed
        }
        return eventList;
    }

    @BeforeMethod
    public void init() {
        eventCount.set(0);
        eventArrived.set(false);
    }

    @AfterMethod
    public void endTest() {
        if (server != null) {
            server.stop();
            server = null;
            log.info("server stopped successfully.");
        }
        receivedEvents.clear();
    }


    /**
     * test for Prometheus source with mandatory fields.
     *
     * @throws InterruptedException interrupted exception
     */
    @Test(sequential = true)
    public void prometheusSourceTest1() throws InterruptedException {

        initializeMetrics(Integer.parseInt(serverPort));
        SiddhiManager siddhiManager = new SiddhiManager();
        log.info("----------------------------------------------------------------------------------");
        log.info("Prometheus Source test with mandatory fields.");
        log.info("----------------------------------------------------------------------------------");
        String metricType = "counter";
        String siddhiApp = "@App:name('TestSiddhiApp1')";
        String sourceStream = "@source(type='prometheus'," +
                "target.url=\'" + targetURL + "\', " +
                "scheme = 'http'," +
                "scrape.interval = '1'," +
                "scrape.timeout = '5'," +
                "metric.type='" + metricType + "'," +
                "metric.name='counter_test'," +
                "@map(type = 'keyvalue'))" +
                "Define stream SourceMapTestStream (metric_name String, metric_type String," +
                " help String, symbol String, price String, subtype String, value int);";
        String outputStream1 = " @sink(type='log')" +
                "define stream OutputStream (metric_name String, metric_type String, help String," +
                " symbol String, price String, subtype String, value int);";
        String query1 = (
                "@info(name = 'query1') "
                        + "from SourceMapTestStream\n" +
                        "select *\n" +
                        "insert into OutputStream;"
        );

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    eventCount.getAndIncrement();
                    eventArrived.set(true);
                    receivedEvents.add(event.getData());
                }
            }
        };
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp + sourceStream +
                outputStream1 + query1);
        siddhiAppRuntime.addCallback("OutputStream", streamCallback);
        StreamCallback insertionStreamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    eventArrived.set(true);

                }
            }
        };
        siddhiAppRuntime.addCallback("SourceMapTestStream", insertionStreamCallback);
        siddhiAppRuntime.start();
        Thread.sleep(2000);

        if (SiddhiTestHelper.isEventsMatch(receivedEvents, retrieveEventList(metricType))) {
            Assert.assertEquals(eventCount.get(), 2);
        } else {
            Assert.fail("Events does not match");
        }
        siddhiAppRuntime.shutdown();
    }

    @Test(sequential = true)
    public void prometheusSourceTest2() throws Exception {
        log.info("----------------------------------------------------------------------------------");
        log.info("Test to verify Prometheus source with custom configuration.");
        log.info("----------------------------------------------------------------------------------");

        String serverPort = System.getenv("SERVER_CONFIG_PORT");
        String host = System.getenv("HOST_NAME");
        String url = "http://" + host + ":" + serverPort;
        initializeMetrics(Integer.parseInt(serverPort));
        Map<String, String> serverConfig = new HashMap<>();
        serverConfig.put("source.prometheus.targetURL", url);
        serverConfig.put("source.prometheus.scrapeInterval", "5");
        InMemoryConfigManager configManager = new InMemoryConfigManager(serverConfig, null);
        configManager.generateConfigReader("source", "prometheus");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setConfigManager(configManager);

        String metricType = "gauge";
        String siddhiApp = "@App:name('TestSiddhiApp2')";
        String sourceStream = "@source(type='prometheus'," +
                "scheme = 'http'," +
                "scrape.timeout = '5'," +
                "metric.type='" + metricType + "'," +
                "metric.name='gauge_test'," +
                "@map(type = 'keyvalue'))" +
                "Define stream SourceMapTestStream (metric_name String, metric_type String," +
                " help String, symbol String, price String, subtype String, value double);";
        String outputStream1 = " @sink(type='log')" +
                "define stream OutputStream (metric_name String, metric_type String, help String," +
                " symbol String, price String, subtype String, value double);";
        String query1 = (
                "@info(name = 'query1') "
                        + "from SourceMapTestStream\n" +
                        "select *\n" +
                        "insert into OutputStream;"
        );

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    eventCount.getAndIncrement();
                    eventArrived.set(true);
                }
            }
        };
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp + sourceStream +
                outputStream1 + query1);
        siddhiAppRuntime.addCallback("OutputStream", streamCallback);
        StreamCallback insertionStreamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    eventArrived.set(true);
                    receivedEvents.add(event.getData());
                }
            }
        };
        siddhiAppRuntime.addCallback("SourceMapTestStream", insertionStreamCallback);
        siddhiAppRuntime.start();

        Thread.sleep(2000);

        if (SiddhiTestHelper.isEventsMatch(receivedEvents, retrieveEventList(metricType))) {
            Assert.assertEquals(eventCount.get(), 2);
        } else {
            Assert.fail("Events does not match");
        }
        siddhiAppRuntime.shutdown();
    }

    /**
     * test for Prometheus source in pause and resume states.
     *
     * @throws InterruptedException
     **/
    @Test(sequential = true)
    public void prometheusSourceTest3() throws InterruptedException, CannotRestoreSiddhiAppStateException {

        log.info("----------------------------------------------------------------------------------");
        log.info("Test to verify the pause and resume functionality of Prometheus source ");
        log.info("----------------------------------------------------------------------------------");

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(new InMemoryPersistenceStore());
        String metricType = "histogram";
        final String sourceStream = "@App:name('prometheusPauseResumeApp') " +
                "@source(type='prometheus'," +
                "scheme = 'http'," +
                "target.url=\'" + targetURL + "\', " +
                "scrape.interval = '1'," +
                "scrape.timeout = '5'," +
                "metric.type='" + metricType + "'," +
                "metric.name='histogram_test'," +
                "@map(type = 'keyvalue'))" +
                "Define stream SourceMapTestStream (metric_name String, metric_type String," +
                " help String, name String, price String, le String, subtype String, value double);";
        final String sinkStream = "@sink(type='prometheus'," +
                "job='sinkTest'," +
                "metric.help= 'test metric'," +
                "publish.mode='server'," +
                "server.url='" + targetURL + "'," +
                "metric.type='" + metricType + "'," +
                "buckets='50, 70, 90'," +
                "metric.name='histogram_test'," +
                "@map(type = 'keyvalue'))" +
                "Define stream TestStream (name String, price int, value double);";
        final String inputStream = "define stream InputStream (name String, price int, value double);\n";
        final String outputStream = " @sink(type='log')" +
                "define stream OutputStream (metric_name String, metric_type String, help String," +
                " name String, price String, le String, subtype String, value double);";
        String query = "@info(name = 'queryInputToPrometheusSink') \n" +
                "from InputStream\n" +
                "select *\n" +
                "insert into TestStream;\n" +
                "\n" +
                "@info(name = 'queryPrometheusSourceToPrometheusSink') \n" +
                "from SourceMapTestStream\n" +
                "select *\n" +
                "insert into OutputStream;\n";


        SiddhiAppRuntime prometheusRecoveryApp =
                siddhiManager.createSiddhiAppRuntime(sourceStream + inputStream + sinkStream + outputStream + query);
        InputHandler inputHandler = prometheusRecoveryApp.getInputHandler("InputStream");
        prometheusRecoveryApp.addCallback("OutputStream", new StreamCallback() {
            @Override
            public synchronized void receive(Event[] events) {
                for (Event event : events) {
                    eventCount.getAndIncrement();
                    eventArrived.set(true);
                }
            }
        });
        // Start the apps
        prometheusRecoveryApp.start();
        Object[] inputEvent1 = new Object[]{"WSO2", 78.8, 100};
        Object[] inputEvent2 = new Object[]{"IBM", 65.32, 125};

        // start receiving events
        Future eventSender = executorService.submit(() -> {
            try {
                inputHandler.send(inputEvent1);
                inputHandler.send(inputEvent2);
            } catch (InterruptedException e) {
                log.error(e);
            }
        });

        while (!eventSender.isDone()) {
            Thread.sleep(1000);
        }
        Thread.sleep(1000);
        Assert.assertEquals(eventCount.get(), 12);
        Assert.assertTrue(eventArrived.get());

        Collection<List<Source>> sources = prometheusRecoveryApp.getSources();
        // pause the transports
        sources.forEach(e -> e.forEach(Source::pause));

        init();
        eventSender = executorService.submit(() -> {
            try {
                inputHandler.send(inputEvent1);
                inputHandler.send(inputEvent2);
            } catch (InterruptedException e) {
                log.error(e);
            }
        });
        while (!eventSender.isDone()) {
            Thread.sleep(1000);
        }

        Thread.sleep(2000);
        Assert.assertFalse(eventArrived.get());

        // resume the transports
        sources.forEach(e -> e.forEach(Source::resume));
        Thread.sleep(2000);
        Assert.assertEquals(eventCount.get(), 12);
        Assert.assertTrue(eventArrived.get());
        prometheusRecoveryApp.shutdown();
    }

    @Test(sequential = true)
    public void prometheusSourceTest4() throws Exception {
        log.info("----------------------------------------------------------------------------------");
        log.info("Test to verify the recovering process of a Siddhi node on a failure in Prometheus source .");
        log.info("----------------------------------------------------------------------------------");


        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        String metricType = "histogram";
        String siddhiApp = "@App:name('prometheusSinkApp') " +
                "@sink(type='prometheus'," +
                "job='sinkTest'," +
                "metric.help= 'test metric'," +
                "publish.mode='server'," +
                "server.url='" + targetURL + "'," +
                "metric.type='" + metricType + "'," +
                "buckets='50, 70, 90'," +
                "metric.name='histogram_test'," +
                "@map(type = 'keyvalue'))" +
                "Define stream TestStream (name String, price int, value double);" +
                "define stream InputStream (name String, price int, value double);\n" +
                " @sink(type='log')" +
                "from InputStream\n" +
                "select *\n" +
                "insert into TestStream;\n";
        String siddhiApp1 = "@App:name('prometheusRecoveryApp') " +
                "@source(type='prometheus'," +
                "scheme = 'http'," +
                "target.url=\'" + targetURL + "\', " +
                "scrape.interval = '10'," +
                "scrape.timeout = '15'," +
                "metric.type='" + metricType + "'," +
                "metric.name='histogram_test'," +
                "@map(type = 'keyvalue'))" +
                "Define stream SourceMapTestStream (metric_name String, metric_type String," +
                " help String, name String, price String, le String, subtype String, value double);" +
                " @sink(type='log')" +
                "define stream OutputStream (metric_name String, metric_type String, help String," +
                " name String, price String, le String, subtype String, value double);" +
                "@info(name = 'queryPrometheusSourceToLog') \n" +
                "from SourceMapTestStream\n" +
                "select *\n" +
                "insert into OutputStream;\n";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");

        SiddhiAppRuntime siddhiAppRuntime1 = siddhiManager.createSiddhiAppRuntime(siddhiApp1);
        siddhiAppRuntime1.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    eventCount.getAndIncrement();
                    eventArrived.set(true);
                }

            }
        });
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Object[] inputEvent1 = new Object[]{"WSO2", 78.8, 100};
        Object[] inputEvent2 = new Object[]{"IBM", 65.32, 125};


        // start publishing events to Prometheus sink
        Future eventSender = executorService.submit(() -> {
            try {
                inputHandler.send(inputEvent1);
                inputHandler.send(inputEvent2);
            } catch (InterruptedException e) {
                log.error(e);
            }
        });
        Thread.sleep(2000);
        // start the siddhi app
        siddhiAppRuntime.start();
        siddhiAppRuntime1.start();

        // wait for some time
        Thread.sleep(2000);
        // initiate a checkpointing task
        Future perisistor = siddhiAppRuntime1.persist().getFuture();
        // waits till the checkpointing task is done
        while (!perisistor.isDone()) {
            Thread.sleep(100);
        }
        // let few more events to be published
        Thread.sleep(2000);
        Assert.assertTrue(eventArrived.get());
        // assert the count
        Assert.assertEquals(12, eventCount.get());
        // initiate a execution app shutdown - to demonstrate a node failure
        siddhiAppRuntime1.shutdown();

        // let few events to be published while the execution app is down
        try {
            inputHandler.send(inputEvent1);
            inputHandler.send(inputEvent2);
        } catch (InterruptedException e) {
            log.error(e);
        }
        Thread.sleep(3000);
        // recreate the siddhi app
        siddhiAppRuntime1 = siddhiManager.createSiddhiAppRuntime(siddhiApp1);
        siddhiAppRuntime1.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    eventCount.getAndIncrement();
                    eventArrived.set(true);
                }

            }
        });
        // start the execution app
        siddhiAppRuntime1.start();
        // immediately trigger a restore from last revision
        siddhiAppRuntime1.restoreLastRevision();
        Thread.sleep(5000);

        // waits till all the events are published
        while (!eventSender.isDone()) {
            Thread.sleep(2000);
        }
        Thread.sleep(6000);
        Assert.assertTrue(eventArrived.get());
        // assert the count
        Assert.assertEquals(24, eventCount.get());
        siddhiAppRuntime.shutdown();
    }
}


