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

import io.prometheus.client.CollectorRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * A singleton class to initialize registry.
 */
class PrometheusRegistryHolder {

    private static Map<Integer, CollectorRegistry> registryMap = new HashMap<>();

    private PrometheusRegistryHolder() {
    }

    private static CollectorRegistry registerRegistry(int hashKey) {
        CollectorRegistry registry = new CollectorRegistry();
        registryMap.put(hashKey, registry);
        return registry;
    }

    static CollectorRegistry retrieveRegistry(String host, int port) {
        int hashKey = (host + port).hashCode();
        if (registryMap.containsKey(hashKey)) {
            return registryMap.get(hashKey);
        } else {
            return registerRegistry(hashKey);
        }
    }
}
