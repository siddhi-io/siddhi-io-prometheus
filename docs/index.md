Siddhi IO Prometheus
===================

  [![Jenkins Build Status](https://wso2.org/jenkins/job/siddhi/job/siddhi-io-prometheus/badge/icon)](https://wso2.org/jenkins/job/siddhi/job/siddhi-io-prometheus/)
  [![GitHub Release](https://img.shields.io/github/release/siddhi-io/siddhi-io-prometheus.svg)](https://github.com/siddhi-io/siddhi-io-prometheus/releases)
  [![GitHub Release Date](https://img.shields.io/github/release-date/siddhi-io/siddhi-io-prometheus.svg)](https://github.com/siddhi-io/siddhi-io-prometheus/releases)
  [![GitHub Open Issues](https://img.shields.io/github/issues-raw/siddhi-io/siddhi-io-prometheus.svg)](https://github.com/siddhi-io/siddhi-io-prometheus/issues)
  [![GitHub Last Commit](https://img.shields.io/github/last-commit/siddhi-io/siddhi-io-prometheus.svg)](https://github.com/siddhi-io/siddhi-io-prometheus/commits/master)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The **siddhi-io-prometheus extension** is an extension to <a target="_blank" href="https://wso2.github.io/siddhi">Siddhi</a> that consumes and expose Prometheus metrics from/to Prometheus server.

For information on <a target="_blank" href="https://siddhi.io/">Siddhi</a> and it's features refer <a target="_blank" href="https://siddhi.io/redirect/docs.html">Siddhi Documentation</a>. 

## Download

* Versions 2.x and above with group id `io.siddhi.extension.*` from <a target="_blank" href="https://mvnrepository.com/artifact/io.siddhi.extension.io.prometheus/siddhi-io-prometheus/">here</a>.
* Versions 1.x and lower with group id `org.wso2.extension.siddhi.*` from <a target="_blank" href="https://mvnrepository.com/artifact/org.wso2.extension.siddhi.io.prometheus/siddhi-io-prometheus">here</a>.

## Latest API Docs 

Latest API Docs is <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-prometheus/api/2.1.0">2.1.0</a>.

## Features

* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-prometheus/api/2.1.0/#prometheus-sink">prometheus</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">Sink</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">This sink publishes events processed by Siddhi into Prometheus metrics and exposes them to the Prometheus server at the specified URL. The created metrics can be published to Prometheus via 'server' or 'pushGateway', depending on your preference.<br>&nbsp;The metric types that are supported by the Prometheus sink are 'counter', 'gauge', 'histogram', and 'summary'. The values and labels of the Prometheus metrics can be updated through the events. </p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-prometheus/api/2.1.0/#prometheus-source">prometheus</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">Source</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">This source consumes Prometheus metrics that are exported from a specified URL as Siddhi events by sending HTTP requests to the URL. Based on the source configuration, it analyzes metrics from the text response and sends them as Siddhi events through key-value mapping.The user can retrieve metrics of the 'including', 'counter', 'gauge', 'histogram', and 'summary' types. The source retrieves the metrics from a text response of the target. Therefore, it is you need to use 'string' as the attribute type for the attributes that correspond with the Prometheus metric labels. Further, the Prometheus metric value is passed through the event as 'value'. This requires you to include an attribute named 'value' in the stream definition. <br>The supported types for the 'value' attribute are 'INT', 'LONG', 'FLOAT', and 'DOUBLE'.</p></p></div>

## Dependencies 

* Prometheus server instance should be started.
* Prometheus Pushgateway should be started. (optional)
* Download and copy the prometheus client jars to the Siddhi Class path from <a target="_blank" href="https://mvnrepository.com/artifact/io.prometheus">
    https://mvnrepository.com/artifact/io.prometheus</a>
    
    * simpleclient_common-*.jar
    * simpleclient-*.jar
    * simpleclient_httpserver-*.jar
    * simpleclient_pushgateway-*.jar
  
## Installation

For installing this extension on various siddhi execution environments refer Siddhi documentation section on <a target="_blank" href="https://siddhi.io/redirect/add-extensions.html">adding extensions</a>.

## Integration Test with Local Docker (Optional)

 * The prometheus sink can be tested with the Docker base integration test framework. The test framework initialize a Docker container with required configuration before execute the test suit.
   
   To start integration tests,
   
   1. Install and run Docker
     
   2. To run the integration tests,
     
      - navigate to the siddhi-io-prometheus/ directory and issue the following command.<br/>
        ```
        mvn verify -P local-prometheus
        ```
           
 * Prometheus target configurations can be modified at the directory for integration tests : 
 
      siddhi-io-prometheus/component/src/test/resources/prometheus/prometheus.yml
      
## Support and Contribution

* We encourage users to ask questions and get support via <a target="_blank" href="https://stackoverflow.com/questions/tagged/siddhi">StackOverflow</a>, make sure to add the `siddhi` tag to the issue for better response.

* If you find any issues related to the extension please report them on <a target="_blank" href="https://github.com/siddhi-io/siddhi-execution-string/issues">the issue tracker</a>.

* For production support and other contribution related information refer <a target="_blank" href="https://siddhi.io/community/">Siddhi Community</a> documentation.
