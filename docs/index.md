Siddhi-io-prometheus
======================================

The **siddhi-io-prometheus extension** is an extension to <a target="_blank" href="https://wso2.github.io/siddhi">Siddhi</a>. The Prometheus-sink publishes Siddhi events as Prometheus metrics and expose them to Prometheus 
server. The Prometheus-source retrieves Prometheus metrics from an endpoint and send them as 
Siddhi events.

## Prerequisites

* Prometheus server instance should be started.
* Prometheus Pushgateway should be started. (optional)
* Download and copy the prometheus client jars to the {WSO2SPHome}/lib directory as follows.
    * Download the following jars from <a target="_blank" href="https://mvnrepository.com/artifact/io.prometheus">
    https://mvnrepository.com/artifact/io.prometheus</a>
        * simpleclient_common-*.jar
        * simpleclient-*.jar
        * simpleclient_httpserver-*.jar
        * simpleclient_pushgateway-*.jar
    * Copy the downloaded jars into {WSO2SPHome}/lib directory.

Find some useful links below:
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-prometheus">Source code</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-prometheus/releases">Releases</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-prometheus/issues">Issue tracker</a>

## Latest API Docs

Latest API Docs is <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-prometheus/api/2.0.1">2.0.1</a>.

## How to use

**Using the extension in <a target="_blank" href="https://github.com/wso2/product-sp">WSO2 Stream Processor</a>**

* You can use this extension in the latest <a target="_blank" href="https://github.com/wso2/product-sp/releases">WSO2 Stream Processor</a> that is a part of <a target="_blank" href="http://wso2.com/analytics?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">WSO2 Analytics</a> offering, with editor, debugger and simulation support.

* This extension is shipped by default with WSO2 Stream Processor, if you wish to use an alternative version of this extension you can replace the component <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-prometheus/releases">jar</a> that can be found in the `<STREAM_PROCESSOR_HOME>/lib` directory.

**Using the extension as a <a target="_blank" href="https://wso2.github.io/siddhi/documentation/running-as-a-java-library">java library</a>**

* This extension can be added as a maven dependency along with other Siddhi dependencies to your project.

```
     <dependency>
        <groupId>io.siddhi.extension.io.prometheus</groupId>
        <artifactId>siddhi-io-prometheus</artifactId>
        <version>x.x.x</version>
     </dependency>
```
## Jenkins Build Status

---

|  Branch | Build Status |
| :------ |:------------ |
| master  | [![Build Status](https://wso2.org/jenkins/job/siddhi/job/siddhi-io-prometheus/badge/icon)](https://wso2.org/jenkins/job/siddhi/job/siddhi-io-prometheus/) |

---

## Features

* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-prometheus/api/2.0.1/#prometheus-sink">prometheus</a> *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#sink">(Sink)</a>*<br><div style="padding-left: 1em;"><p>This sink publishes events processed by Siddhi into Prometheus metrics and exposes them to the Prometheus server at the specified URL. The created metrics can be published to Prometheus via 'server' or 'pushGateway', depending on your preference.<br>&nbsp;The metric types that are supported by the Prometheus sink are 'counter', 'gauge', 'histogram', and 'summary'. The values and labels of the Prometheus metrics can be updated through the events. </p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-prometheus/api/2.0.1/#prometheus-source">prometheus</a> *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#source">(Source)</a>*<br><div style="padding-left: 1em;"><p>This source consumes Prometheus metrics that are exported from a specified URL as Siddhi events by sending HTTP requests to the URL. Based on the source configuration, it analyzes metrics from the text response and sends them as Siddhi events through key-value mapping.The user can retrieve metrics of the 'including', 'counter', 'gauge', 'histogram', and 'summary' types. The source retrieves the metrics from a text response of the target. Therefore, it is you need to use 'string' as the attribute type for the attributes that correspond with the Prometheus metric labels. Further, the Prometheus metric value is passed through the event as 'value'. This requires you to include an attribute named 'value' in the stream definition. <br>The supported types for the 'value' attribute are 'INT', 'LONG', 'FLOAT', and 'DOUBLE'.</p></div>

## How to contribute
* Report issues at <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-prometheus/issues">GitHub Issue Tracker</a>.

* Send your contributions as pull requests to the <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-prometheus">master branch</a>.

## Running Integration tests in docker containers (Optional)
 * The prometheus sink can be tested with the docker base integration test framework. The test framework initialize a docker container with required configuration before execute the test suit.
    
   To start integration tests,
   
     1. Install and run docker
     
     2. To run the integration tests,
     
         - navigate to the siddhi-io-prometheus/ directory and issue the following command.
           ```
           mvn verify -P local-prometheus
           ```
 * Prometheus target configurations can be modified at the directory for integration tests : 
 
      siddhi-io-prometheus/component/src/test/resources/prometheus/prometheus.yml
     
## Contact us
 * Post your questions with the <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">"Siddhi"</a> tag in <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">Stackoverflow</a>.


 * Siddhi developers can be contacted via the mailing lists:

    Developers List   : [dev@wso2.org](mailto:dev@wso2.org)

    Architecture List : [architecture@wso2.org](mailto:architecture@wso2.org)

## Support
* We are committed to ensuring support for this extension in production. Our unique approach ensures that all support leverages our open development methodology and is provided by the very same engineers who build the technology.

* For more details and to take advantage of this unique opportunity contact us via <a target="_blank" href="http://wso2.com/support?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">http://wso2.com/support/</a>.
