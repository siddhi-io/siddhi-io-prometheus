??Siddhi-io-prometheus
======================================

The **siddhi-io-prometheus extension** is an extension to <a target="_blank" href="https://wso2.github.io/siddhi">Siddhi</a>. The Prometheus-sink publishes Siddhi events as Prometheus metrics and expose them to Prometheus 
server. The Prometheus-source retrieves Prometheus metrics from an endpoint and send them as 
Siddhi events.

## Prerequisites

* Prometheus server instance should be started.
* Prometheus Pushgateway should be started. (optional)

Find some useful links below:
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-prometheus">Source code</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-prometheus/releases">Releases</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-prometheus/issues">Issue tracker</a>

## Latest API Docs

Latest API Docs is <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-prometheus/api/1.0.0">1.0.0</a>.

## How to use

**Using the extension in <a target="_blank" href="https://github.com/wso2/product-sp">WSO2 Stream Processor</a>**

* You can use this extension in the latest <a target="_blank" href="https://github.com/wso2/product-sp/releases">WSO2 Stream Processor</a> that is a part of <a target="_blank" href="http://wso2.com/analytics?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">WSO2 Analytics</a> offering, with editor, debugger and simulation support.

* This extension is shipped by default with WSO2 Stream Processor, if you wish to use an alternative version of this extension you can replace the component <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-prometheus/releases">jar</a> that can be found in the `<STREAM_PROCESSOR_HOME>/lib` directory.

**Using the extension as a <a target="_blank" href="https://wso2.github.io/siddhi/documentation/running-as-a-java-library">java library</a>**

* This extension can be added as a maven dependency along with other Siddhi dependencies to your project.

```
     <dependency>
        <groupId>org.wso2.extension.siddhi.io.prometheus</groupId>
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

* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-prometheus/api/1.0.0/#prometheus-sink">prometheus</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#sink">(Sink)</a>*<br><div style="padding-left: 1em;"><p>The sink publishes events processed by WSO2 SP into Prometheus metrics and exposes them to Prometheus server at the provided url. The created metrics can be published to Prometheus through 'server' or 'pushGateway' publishing modes depending on the preference of the user. The server mode exposes the metrics through an http server at the provided url and the pushGateway mode pushes the metrics to pushGateway which must be running at the provided url.<br>&nbsp;The metric types that are supported by Prometheus sink are counter, gauge, histogram and summary. The values and labels of the Prometheus metrics can be updated through the events. </p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-prometheus/api/1.0.0/#prometheus-source">prometheus</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#source">(Source)</a>*<br><div style="padding-left: 1em;"><p>The source consumes Prometheus metrics which are exported from the specified url as Siddhi events, by making http requests to the url. According to the source configuration, it analyses metrics from the text response and sends them as Siddhi events through key-value mapping.The user can retrieve metrics of types including, counter, gauge, histogram and summary. Since the source retrieves the metrics from a text response of the target, it is advised to use 'string' as the attribute type for the attributes that correspond to Prometheus metric labels. Further, the Prometheus metric value is passed through the event as 'value'. Therefore, it is advisable to have an attribute with the name 'value' in the stream. <br>The supported types for the attribute, 'value' are INT, LONG, FLOAT and DOUBLE.</p></div>

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
