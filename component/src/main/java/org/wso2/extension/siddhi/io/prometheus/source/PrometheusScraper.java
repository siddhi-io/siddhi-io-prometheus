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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants;
import org.wso2.extension.siddhi.io.prometheus.util.PrometheusSourceUtil;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.wso2.extension.siddhi.io.prometheus.util.PrometheusConstants.EMPTY_STRING;

/**
 * This class creates and sends an http request to the target-URL according to user inputs. And it transfers the
 * retrieved data to {@code PrometheusMetricAnalyser} class.
 */
public class PrometheusScraper implements Runnable {
    private static final Logger log = Logger.getLogger(PrometheusScraper.class);
    private final String targetURL;
    private final long scrapeTimeout;
    private final String scheme;
    private final List<Header> headers;
    private final SourceEventListener sourceEventListener;
    private final String streamName;
    private boolean isPaused = false;
    private List<String> metricSamples = new ArrayList<>();
    private HttpClientConnector httpClientConnector;
    private Map<String, String> urlProperties;
    private String userName = EMPTY_STRING;
    private String password = EMPTY_STRING;
    private String clientStoreFile;
    private String clientStorePassword;
    private List<String> lastValidSamples;
    private CompletionCallback completionCallback;
    private PrometheusMetricAnalyser metricAnalyser;
    private HttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();
    private HTTPCarbonMessage httpRequest;

    PrometheusScraper(String targetURL, String scheme, long scrapeTimeout,
                      List<Header> headers, SourceEventListener sourceEventListener, String streamName) {
        this.targetURL = targetURL;
        this.scheme = scheme;
        this.scrapeTimeout = scrapeTimeout;
        this.headers = headers;
        this.sourceEventListener = sourceEventListener;
        this.streamName = streamName;
    }

    void setMetricProperties(String metricName, MetricType metricType, String metricJob,
                             String metricInstance, Map<String, String> metricGroupingKey,
                             Attribute.Type valueType) {
        this.metricAnalyser = new PrometheusMetricAnalyser(metricName, metricType, metricJob, metricInstance,
                metricGroupingKey, valueType, sourceEventListener);
    }


    void setAuthorizationCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    void setHttpsProperties(String clientStoreFile, String clientStorePassword) {
        this.clientStoreFile = clientStoreFile;
        this.clientStorePassword = clientStorePassword;

    }

    void setCompletionCallback(CompletionCallback completionCallback) {
        this.completionCallback = completionCallback;
    }

    void createConnectionChannel() {
        try {
            urlProperties = PrometheusSourceUtil.getURLProperties(targetURL, scheme);
        } catch (MalformedURLException e) {
            //target URL is already validated.
        }
        SenderConfiguration senderConfiguration = PrometheusSourceUtil.getSenderConfigurations(urlProperties,
                clientStoreFile, clientStorePassword);
        senderConfiguration.setSocketIdleTimeout((int) (scrapeTimeout * 1000));
        if (!(PrometheusSourceUtil.checkEmptyString(userName) || PrometheusSourceUtil.checkEmptyString(password))) {
            String basicAuthHeader = "Basic " + encode(userName + ":" + password);
            headers.add(new Header(PrometheusConstants.AUTHORIZATION_HEADER, basicAuthHeader));
        }
        httpClientConnector = httpConnectorFactory.createHttpClientConnector(new HashMap<>(),
                senderConfiguration);
    }

    private void retrieveMetricSamples() throws ConnectionUnavailableException {
        List<String> responseMetrics = sendRequest();
        String errorMessage = null;
        if (responseMetrics == null) {
            errorMessage = "Error occurred while retrieving metrics at " + targetURL + ". Error : Response is null.";
        } else {
            if (responseMetrics.isEmpty()) {
                errorMessage = "The target at " + targetURL + " returns an empty response";
            }
        }
        if (errorMessage != null) {
            log.error(errorMessage, new SiddhiAppRuntimeException(errorMessage));
        }
        if (!responseMetrics.equals(metricSamples)) {
            metricSamples = responseMetrics;
            metricAnalyser.analyseMetrics(metricSamples, targetURL, streamName);
            this.lastValidSamples = metricAnalyser.getLastValidSamples();
        }
    }

    private String encode(String userNamePassword) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(userNamePassword.getBytes(StandardCharsets.UTF_8));
        ByteBuf encodedByteBuf = Base64.encode(byteBuf);
        return encodedByteBuf.toString(StandardCharsets.UTF_8);
    }

    private List<String> sendRequest() throws ConnectionUnavailableException {
        List<String> responsePayload = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        HTTPCarbonMessage carbonMessage = generateCarbonMessage();
        HttpResponseFuture httpResponseFuture = httpClientConnector.send(carbonMessage);
        PrometheusHTTPClientListener httpListener = new PrometheusHTTPClientListener(latch);
        httpResponseFuture.setHttpConnectorListener(httpListener);
        BufferedReader bufferedReader = null;
        try {
            if (latch.await(scrapeTimeout + 10, TimeUnit.SECONDS)) {
                HTTPCarbonMessage response = httpListener.getHttpResponseMessage();
                bufferedReader = new BufferedReader(new InputStreamReader(
                        new HttpMessageDataStreamer(response).getInputStream(), Charset.defaultCharset()));
                int statusCode = response.getNettyHttpResponse().status().code();
                if (statusCode == 200) {
                    responsePayload = bufferedReader.lines().collect(Collectors.toList());
                } else {
                    String errorMessage = "Error occurred while retrieving metrics. HTTP error code: " +
                            statusCode;
                    throw new ConnectionUnavailableException(errorMessage);
                }
            }
        } catch (InterruptedException e) {
            log.error(" Interrupted exception thrown in " + PrometheusConstants.PROMETHEUS_SOURCE + " associated with" +
                    " stream " + streamName + " while sending request.", e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.error(" IO exception thrown in " + PrometheusConstants.PROMETHEUS_SOURCE + " associated with" +
                            " stream " + streamName + " while closing the Buffered reader.", e);
                }
            }
        }
        return responsePayload;
    }

    private HTTPCarbonMessage generateCarbonMessage() {
        HttpMethod httpReqMethod = new HttpMethod(PrometheusConstants.DEFAULT_HTTP_METHOD);
        HTTPCarbonMessage carbonMessage = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                httpReqMethod, EMPTY_STRING));
        carbonMessage.setProperty(Constants.PROTOCOL, urlProperties.get(Constants.PROTOCOL));
        carbonMessage.setProperty(Constants.TO, urlProperties.get(Constants.TO));
        carbonMessage.setProperty(Constants.HTTP_HOST, urlProperties.get(Constants.HTTP_HOST));
        carbonMessage.setProperty(Constants.HTTP_PORT, Integer.valueOf(urlProperties.get(Constants.HTTP_PORT)));
        carbonMessage.setProperty(Constants.HTTP_METHOD, PrometheusConstants.DEFAULT_HTTP_METHOD);
        carbonMessage.setProperty(Constants.REQUEST_URL, urlProperties.get(Constants.REQUEST_URL));
        HttpHeaders httpHeaders = carbonMessage.getHeaders();
        httpHeaders.set(Constants.HTTP_HOST, carbonMessage.getProperty(Constants.HTTP_HOST));
        if (headers != null) {
            for (Header header : headers) {
                httpHeaders.set(header.getName(), header.getValue());
            }
        }
        httpHeaders.set(PrometheusConstants.HTTP_CONTENT_TYPE, PrometheusConstants.TEXT_PLAIN);
        httpHeaders.set(PrometheusConstants.HTTP_METHOD, PrometheusConstants.DEFAULT_HTTP_METHOD);
        carbonMessage.completeMessage();
        return carbonMessage;
    }

    @Override
    public void run() {
        if (!isPaused) {
            try {
                retrieveMetricSamples();
            } catch (ConnectionUnavailableException e) {
                completionCallback.handle(e);
            }
        }
    }

    void pause() {
        isPaused = true;
    }

    void resume() {
        isPaused = false;
    }

    List<String> getLastValidResponse() {
        return this.lastValidSamples;
    }

    void setLastValidResponse(List<String> lastValidResponse) {
        this.lastValidSamples = lastValidResponse;
    }

    void clearPrometheusScraper() {
        if (metricSamples != null) {
            metricSamples.clear();
        }
        if (lastValidSamples != null) {
            lastValidSamples.clear();
        }
    }

    void clearConnectorFactory() {
        try {
            httpConnectorFactory.shutdown();
        } catch (InterruptedException e) {
            log.error(" Interrupted exception thrown in " + PrometheusConstants.PROMETHEUS_SOURCE + " associated with" +
                    " stream " + streamName + " while disconnecting.", e);
        }
    }

    /**
     * A callback function to be notified when {@code PrometheusScraper} throws an Error.
     */
    public interface CompletionCallback {
        /**
         * Handle errors from {@link PrometheusScraper}.
         *
         * @param error the error.
         */
        void handle(Throwable error);
    }
}
