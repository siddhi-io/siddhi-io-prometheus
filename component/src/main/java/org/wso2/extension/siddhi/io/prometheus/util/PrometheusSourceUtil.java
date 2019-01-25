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

import org.wso2.carbon.messaging.Header;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.SenderConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code PrometheusSourceUtil } responsible for util functions of Prometheus-source.
 */
public class PrometheusSourceUtil {

    public static boolean checkEmptyString(String stringValue) {
        return PrometheusConstants.EMPTY_STRING.equals(stringValue.trim());
    }

    public static Map<String, String> getURLProperties(String target, String scheme) throws MalformedURLException {
        URL targetURL = new URL(target);
        Map<String, String> httpURLProperties;
        httpURLProperties = new HashMap<>();
        httpURLProperties.put(Constants.TO, targetURL.getFile());
        String protocol = targetURL.getProtocol();
        httpURLProperties.put(Constants.PROTOCOL, protocol);
        httpURLProperties.put(Constants.HTTP_HOST, targetURL.getHost());
        int port;
        if (Constants.HTTPS_SCHEME.equalsIgnoreCase(protocol)) {
            port = targetURL.getPort() != -1 ? targetURL.getPort() : PrometheusConstants.DEFAULT_HTTPS_PORT;
        } else {
            port = targetURL.getPort() != -1 ? targetURL.getPort() : PrometheusConstants.DEFAULT_HTTP_PORT;
        }
        httpURLProperties.put(Constants.HTTP_PORT, Integer.toString(port));
        httpURLProperties.put(Constants.REQUEST_URL, targetURL.toString());
        return httpURLProperties;
    }

    public static String trustStorePath(ConfigReader configReader) {
        return configReader.readConfig(PrometheusConstants.TRUSTSTORE_FILE_CONFIGURATION,
                PrometheusConstants.TRUSTSTORE_PATH_VALUE);
    }

    public static String trustStorePassword(ConfigReader configReader) {
        return configReader.readConfig(PrometheusConstants.TRUSTSTORE_PASSWORD_CONFIGURATION,
                PrometheusConstants.TRUSTSTORE_PASSWORD_VALUE);
    }

    public static SenderConfiguration getSenderConfigurations(Map<String, String> urlProperties, String clientStoreFile,
                                                              String clientStorePass) {
        SenderConfiguration httpSender = new SenderConfiguration(urlProperties
                .get(Constants.HTTP_PORT));
        if (urlProperties.get(Constants.PROTOCOL).equals(PrometheusConstants.HTTPS_SCHEME)) {
            httpSender.setTrustStoreFile(clientStoreFile);
            httpSender.setTrustStorePass(clientStorePass);
            httpSender.setId(urlProperties.get(Constants.TO));
        }
        httpSender.setScheme(urlProperties.get(Constants.PROTOCOL));
        return httpSender;
    }

    /**
     * Method is responsible of to convert string of headers to list of headers.
     * Example header format : 'name1:value1','name2:value2'
     *
     * @param headers string of headers list.
     * @return list of headers.
     */
    public static List<Header> getHeaders(String headers, String streamID) {
        if (PrometheusSourceUtil.checkEmptyString(headers)) {
            return null;
        }
        List<Header> headersList = new ArrayList<>();
        String[] headerArray = headers.trim().split(PrometheusConstants.KEY_VALUE_SEPARATOR);
        for (String headerValue : headerArray) {
            headerValue = headerValue.substring(1, headerValue.length() - 1);
            String[] header = headerValue.split(PrometheusConstants.VALUE_SEPARATOR, 2);
            if (header.length <= 1) {
                throw new SiddhiAppCreationException(
                        "Invalid header format found in " + PrometheusConstants.PROMETHEUS_SOURCE + " " +
                                "associated with stream \'" + streamID + "\'. Please include them as " +
                                "'key1:value1', 'key2:value2',..");
            }
            headersList.add(new Header(header[0], header[1]));
        }
        return headersList;
    }

    /**
     * Method is responsible of to convert string of key value pairs to map of Strings.
     * Example input format : 'name1:value1','name2:value2'
     *
     * @param stringInput string of key value pairs.
     * @return map of stringInput.
     */
    public static Map<String, String> populateStringMap(String stringInput, String streamName) {
        Map<String, String> stringMap = new HashMap<>();
        if (PrometheusSourceUtil.checkEmptyString(stringInput)) {
            return stringMap;
        }
        String[] stringArray = stringInput.trim().split(PrometheusConstants.KEY_VALUE_SEPARATOR);
        Arrays.stream(stringArray).forEach(valueEntry -> {
            String[] entry =
                    valueEntry.substring(1, valueEntry.length() - 1).split(PrometheusConstants.VALUE_SEPARATOR, 2);
            if (entry.length != 2) {
                throw new SiddhiAppCreationException(
                        "Invalid format for key-value input in " + streamName + " of " +
                                PrometheusConstants.PROMETHEUS_SOURCE + ". Please include as " +
                                "'key1:value1','key2:value2',..");
            }
            stringMap.put(entry[0], entry[1]);
        });
        return stringMap;
    }
}
