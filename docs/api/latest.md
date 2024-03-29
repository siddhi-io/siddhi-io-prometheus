# API Docs - v2.1.3

!!! Info "Tested Siddhi Core version: *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/">5.1.4</a>*"
    It could also support other Siddhi Core minor versions.

## Sink

### prometheus *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">This sink publishes events processed by Siddhi into Prometheus metrics and exposes them to the Prometheus server at the specified URL. The created metrics can be published to Prometheus via 'server' or 'pushGateway', depending on your preference.<br>&nbsp;The metric types that are supported by the Prometheus sink are 'counter', 'gauge', 'histogram', and 'summary'. The values and labels of the Prometheus metrics can be updated through the events. </p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="prometheus", job="<STRING>", publish.mode="<STRING>", push.url="<STRING>", server.url="<STRING>", metric.type="<STRING>", metric.help="<STRING>", metric.name="<STRING>", buckets="<STRING>", quantiles="<STRING>", quantile.error="<DOUBLE>", value.attribute="<STRING>", push.operation="<STRING>", grouping.key="<STRING>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">job</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This parameter specifies the job name of the metric. This must be the same job name that is defined in the Prometheus configuration file.</p></td>
        <td style="vertical-align: top">siddhiJob</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">publish.mode</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The mode in which the metrics need to be exposed to the Prometheus server.The possible publishing modes are 'server' and 'pushgateway'.The server mode exposes the metrics through an HTTP server at the specified URL, and the 'pushGateway' mode pushes the metrics to the pushGateway that needs to be running at the specified URL.</p></td>
        <td style="vertical-align: top">server</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">push.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This parameter specifies the target URL of the Prometheus pushGateway. This is the URL at which the pushGateway must be listening. This URL needs to be defined in the Prometheus configuration file as a target before it can be used here.</p></td>
        <td style="vertical-align: top">http://localhost:9091</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">server.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This parameter specifies the URL where the HTTP server is initiated to expose metrics in the 'server' publish mode. This URL needs to be defined in the Prometheus configuration file as a target before it can be used here.</p></td>
        <td style="vertical-align: top">http://localhost:9080</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">metric.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The type of Prometheus metric that needs to be created at the sink.<br>&nbsp;The supported metric types are 'counter', 'gauge',c'histogram' and 'summary'. </p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">metric.help</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">A brief description of the metric and its purpose.</p></td>
        <td style="vertical-align: top"><metric_name_with_metric_type></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">metric.name</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This parameter allows you to assign a preferred name for the metric. The metric name must match the regex format, i.e., [a-zA-Z_:][a-zA-Z0-9_:]*. </p></td>
        <td style="vertical-align: top"><stream_name></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">buckets</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The bucket values preferred by the user for histogram metrics. The bucket values must be in the 'string' format with each bucket value separated by a comma as shown in the example below.<br>"2,4,6,8"</p></td>
        <td style="vertical-align: top">null</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">quantiles</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This parameter allows you to specify quantile values for summary metrics as preferred. The quantile values must be in the 'string' format with each quantile value separated by a comma as shown in the example below.<br>"0.5,0.75,0.95"</p></td>
        <td style="vertical-align: top">null</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">quantile.error</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The error tolerance value for calculating quantiles in summary metrics. This must be a positive value, but less than 1.</p></td>
        <td style="vertical-align: top">0.001</td>
        <td style="vertical-align: top">DOUBLE</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">value.attribute</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The name of the attribute in the stream definition that specifies the metric value. The defined 'value' attribute must be included in the stream definition. The system increases the metric value for the counter and gauge metric types by the value of the 'value attribute. The system observes the value of the 'value' attribute for the calculations of 'summary' and 'histogram' metric types.</p></td>
        <td style="vertical-align: top">value</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">push.operation</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This parameter defines the mode for pushing metrics to the pushGateway. The available push operations are 'push' and 'pushadd'. The operations differ according to the existing metrics in pushGateway where 'push' operation replaces the existing metrics, and 'pushadd' operation only updates the newly created metrics.</p></td>
        <td style="vertical-align: top">pushadd</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">grouping.key</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This parameter specifies the grouping key of created metrics in key-value pairs. The grouping key is used only in pushGateway mode in order to distinguish the metrics from already existing metrics. <br>The expected format of the grouping key is as follows:<br>&nbsp;"'key1:value1','key2:value2'"</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">jobName</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">This property specifies the default job name for the metric. This job name must be the same as the job name defined in the Prometheus configuration file.</p></td>
        <td style="vertical-align: top">siddhiJob</td>
        <td style="vertical-align: top">Any string</td>
    </tr>
    <tr>
        <td style="vertical-align: top">publishMode</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default publish mode for the Prometheus sink for exposing metrics to the Prometheus server. The mode can be either 'server' or 'pushgateway'. </p></td>
        <td style="vertical-align: top">server</td>
        <td style="vertical-align: top">server or pushgateway</td>
    </tr>
    <tr>
        <td style="vertical-align: top">serverURL</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">This property configures the URL where the HTTP server is initiated to expose metrics. This URL needs to be defined in the Prometheus configuration file as a target to be identified by Prometheus before it can be used here. By default, the HTTP server is initiated at 'http://localhost:9080'.</p></td>
        <td style="vertical-align: top">http://localhost:9080</td>
        <td style="vertical-align: top">Any valid URL</td>
    </tr>
    <tr>
        <td style="vertical-align: top">pushURL</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">This property configures the target URL of the Prometheus pushGateway (where the pushGateway needs to listen). This URL needs to be defined in the Prometheus configuration file as a target to be identified by Prometheus before it can be used here.</p></td>
        <td style="vertical-align: top">http://localhost:9091</td>
        <td style="vertical-align: top">Any valid URL</td>
    </tr>
    <tr>
        <td style="vertical-align: top">groupingKey</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">This property configures the grouping key of created metrics in key-value pairs. Grouping key is used only in pushGateway mode in order to distinguish these metrics from already existing metrics under the same job. The expected format of the grouping key is as follows: "'key1:value1','key2:value2'" .</p></td>
        <td style="vertical-align: top">null</td>
        <td style="vertical-align: top">Any key value pairs in the supported format</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='prometheus',job='fooOrderCount', server.url ='http://localhost:9080', publish.mode='server', metric.type='counter', metric.help= 'Number of foo orders', @map(type='keyvalue'))
define stream FooCountStream (Name String, quantity int, value int);

```
<p></p>
<p style="word-wrap: break-word;margin: 0;"> In the above example, the Prometheus-sink creates a counter metric with the stream name and defined attributes as labels. The metric is exposed through an HTTP server at the target URL.</p>
<p></p>
<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@sink(type='prometheus',job='inventoryLevel', push.url='http://localhost:9080', publish.mode='pushGateway', metric.type='gauge', metric.help= 'Current level of inventory', @map(type='keyvalue'))
define stream InventoryLevelStream (Name String, value int);

```
<p></p>
<p style="word-wrap: break-word;margin: 0;"> In the above example, the Prometheus-sink creates a gauge metric with the stream name and defined attributes as labels.The metric is pushed to the Prometheus pushGateway at the target URL.</p>
<p></p>
## Source

### prometheus *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">This source consumes Prometheus metrics that are exported from a specified URL as Siddhi events by sending HTTP requests to the URL. Based on the source configuration, it analyzes metrics from the text response and sends them as Siddhi events through key-value mapping.The user can retrieve metrics of the 'including', 'counter', 'gauge', 'histogram', and 'summary' types. The source retrieves the metrics from a text response of the target. Therefore, it is you need to use 'string' as the attribute type for the attributes that correspond with the Prometheus metric labels. Further, the Prometheus metric value is passed through the event as 'value'. This requires you to include an attribute named 'value' in the stream definition. <br>The supported types for the 'value' attribute are 'INT', 'LONG', 'FLOAT', and 'DOUBLE'.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@source(type="prometheus", target.url="<STRING>", scrape.interval="<INT>", scrape.timeout="<INT>", scheme="<STRING>", metric.name="<STRING>", metric.type="<STRING>", username="<STRING>", password="<STRING>", client.truststore.file="<STRING>", client.truststore.password="<STRING>", headers="<STRING>", job="<STRING>", instance="<STRING>", grouping.key="<STRING>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">target.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This property specifies the target URL to which the Prometheus metrics are exported in the 'TEXT' format.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">scrape.interval</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This property specifies the time interval in seconds within which the source should send an HTTP request to the specified target URL.</p></td>
        <td style="vertical-align: top">60</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">scrape.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This property is the time duration in seconds for a scrape request to get timed-out if the server at the URL does not respond.</p></td>
        <td style="vertical-align: top">10</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">scheme</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This property specifies the scheme of the target URL.<br>&nbsp;The supported schemes are 'HTTP' and 'HTTPS'.</p></td>
        <td style="vertical-align: top">HTTP</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">metric.name</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This property specifies the name of the metrics that are to be fetched. The metric name must match the regex format, i.e., '[a-zA-Z_:][a-zA-Z0-9_:]* '.</p></td>
        <td style="vertical-align: top">Stream name</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">metric.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This property specifies the type of the Prometheus metric that is required to be fetched. <br>&nbsp;The supported metric types are 'counter', 'gauge'," 'histogram', and 'summary'. </p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This property specifies the username that needs to be added in the authorization header of the HTTP request if basic authentication is enabled at the target. It is required to specify both the username and the password to enable basic authentication. If you do not provide a value for one or both of these parameters, an error is logged in the console.</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This property specifies the password that needs to be added in the authorization header of the HTTP request if basic authentication is enabled at the target. It is required to specify both the username and the password to enable basic authentication. If you do not provide a value for one or both of these parameters, an error is logged in the console.</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">client.truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The file path to the location of the truststore to which the client needs to send HTTPS requests via the 'HTTPS' protocol.</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">client.truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;"> The password for the client-truststore. This is required to send HTTPS requests. A custom password can be specified if required. </p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Headers that need to be included as HTTP request headers in the request. <br>The format of the supported input is as follows, <br>"'header1:value1','header2:value2'"</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">job</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;"> This property defines the job name of the exported Prometheus metrics that needs to be fetched.</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">instance</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This property defines the instance of the exported Prometheus metrics that needs to be fetched.</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">grouping.key</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This parameter specifies the grouping key of the required metrics in key-value pairs. The grouping key is used if the metrics are exported by Prometheus 'pushGateway' in order to distinguish those metrics from already existing metrics.<br>&nbsp;The expected format of the grouping key is as follows: <br>"'key1:value1','key2:value2'"</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">scrapeInterval</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default time interval in seconds for the Prometheus source to send HTTP requests to the target URL.</p></td>
        <td style="vertical-align: top">60</td>
        <td style="vertical-align: top">Any integer value</td>
    </tr>
    <tr>
        <td style="vertical-align: top">scrapeTimeout</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default time duration (in seconds) for an HTTP request to time-out if the server at the URL does not respond. </p></td>
        <td style="vertical-align: top">10</td>
        <td style="vertical-align: top">Any integer value</td>
    </tr>
    <tr>
        <td style="vertical-align: top">scheme</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The scheme of the target for the Prometheus source to send HTTP requests. The supported schemes are 'HTTP' and 'HTTPS'.</p></td>
        <td style="vertical-align: top">HTTP</td>
        <td style="vertical-align: top">HTTP or HTTPS</td>
    </tr>
    <tr>
        <td style="vertical-align: top">username</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The username that needs to be added in the authorization header of the HTTP request if basic authentication is enabled at the target. It is required to specify both the username and password to enable basic authentication. If you do not specify a value for one or both of these parameters, an error is logged in the console.</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">Any string</td>
    </tr>
    <tr>
        <td style="vertical-align: top">password</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The password that needs to be added in the authorization header of the HTTP request if basic authentication is enabled at the target. It is required to specify both the username and password to enable basic authentication. If you do not specify a value for one or both of these parameters, an error is logged in the console.</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">Any string</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trustStoreFile</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default file path to the location of truststore that the client needs to access in order to send HTTPS requests through 'HTTPS' protocol.</p></td>
        <td style="vertical-align: top">${carbon.home}/resources/security/client-truststore.jks</td>
        <td style="vertical-align: top">Any valid path for the truststore file</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trustStorePassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default password for the client-truststore that the client needs to access in order to send HTTPS requests through 'HTTPS' protocol.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Any string</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The headers that need to be included as HTTP request headers in the scrape request. <br>The format of the supported input is as follows, <br>"'header1:value1','header2:value2'"</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">Any valid http headers</td>
    </tr>
    <tr>
        <td style="vertical-align: top">job</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;"> The default job name of the exported Prometheus metrics that needs to be fetched.</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">Any valid job name</td>
    </tr>
    <tr>
        <td style="vertical-align: top">instance</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default instance of the exported Prometheus metrics that needs to be fetched.</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">Any valid instance name</td>
    </tr>
    <tr>
        <td style="vertical-align: top">groupingKey</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default grouping key of the required Prometheus metrics in key-value pairs. The grouping key is used if the metrics are exported by the Prometheus pushGateway in order to distinguish these metrics from already existing metrics. <br>The expected format of the grouping key is as follows: <br>"'key1:value1','key2:value2'"</p></td>
        <td style="vertical-align: top"><empty_string></td>
        <td style="vertical-align: top">Any valid grouping key pairs</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type= 'prometheus', target.url= 'http://localhost:9080/metrics', metric.type= 'counter', metric.name= 'sweet_production_counter', @map(type= 'keyvalue'))
define stream FooStream1(metric_name string, metric_type string, help string, subtype string, name string, quantity string, value double);

```
<p></p>
<p style="word-wrap: break-word;margin: 0;">In this example, the Prometheus source sends an HTTP request to the 'target.url' and analyzes the response. From the analyzed response, the source retrieves the Prometheus counter metrics with the 'sweet_production_counter' nameand converts the filtered metrics into Siddhi events using the key-value mapper.<br>The generated maps have keys and values as follows: <br>&nbsp;&nbsp;metric_name  -&gt; sweet_production_counter<br>&nbsp;&nbsp;metric_type  -&gt; counter<br>&nbsp;&nbsp;help  -&gt; &lt;help_string_of_metric&gt;<br>&nbsp;&nbsp;subtype  -&gt; null<br>&nbsp;&nbsp;name -&gt; &lt;value_of_label_name&gt;<br>&nbsp;&nbsp;quantity -&gt; &lt;value_of_label_quantity&gt;<br>&nbsp;&nbsp;value -&gt; &lt;value_of_metric&gt;<br></p>
<p></p>
<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@source(type= 'prometheus', target.url= 'http://localhost:9080/metrics', metric.type= 'summary', metric.name= 'sweet_production_summary', @map(type= 'keyvalue'))
 define stream FooStream2(metric_name string, metric_type string, help string, subtype string, name string, quantity string, quantile string, value double);

```
<p></p>
<p style="word-wrap: break-word;margin: 0;">In this example, the Prometheus source sends an HTTP request to the 'target.url' and analyzes the response. From the analysed response, the source retrieves the Prometheus summary metrics with the 'sweet_production_summary' nameand converts the filtered metrics into Siddhi events using the key-value mapper.<br>The generated maps have keys and values as follows: <br>&nbsp;&nbsp;metric_name  -&gt; sweet_production_summary<br>&nbsp;&nbsp;metric_type  -&gt; summary<br>&nbsp;&nbsp;help  -&gt; &lt;help_string_of_metric&gt;<br>&nbsp;&nbsp;subtype  -&gt; &lt;'sum'/'count'/'null'&gt;<br>&nbsp;&nbsp;name -&gt; &lt;value_of_label_name&gt;<br>&nbsp;&nbsp;quantity -&gt; &lt;value_of_label_quantity&gt;<br>&nbsp;&nbsp;quantile  -&gt; &lt;value of the quantile&gt;<br>&nbsp;&nbsp;value -&gt; &lt;value_of_metric&gt;<br></p>
<p></p>
<span id="example-3" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 3</span>
```
@source(type= 'prometheus', target.url= 'http://localhost:9080/metrics', metric.type= 'histogram', metric.name= 'sweet_production_histogram', @map(type= 'keyvalue'))
define stream FooStream3(metric_name string, metric_type string, help string, subtype string, name string, quantity string, le string, value double);

```
<p></p>
<p style="word-wrap: break-word;margin: 0;">In this example, the prometheus source sends an HTTP request to the 'target.url' and analyzes the response. From the analyzed response, the source retrieves the Prometheus histogram metrics with the 'sweet_production_histogram' name and converts the filtered metrics into Siddhi events using the key-value mapper.<br>The generated maps have keys and values as follows, <br>&nbsp;&nbsp;metric_name  -&gt; sweet_production_histogram<br>&nbsp;&nbsp;metric_type  -&gt; histogram<br>&nbsp;&nbsp;help  -&gt; &lt;help_string_of_metric&gt;<br>&nbsp;&nbsp;subtype  -&gt; &lt;'sum'/'count'/'bucket'&gt;<br>&nbsp;&nbsp;name -&gt; &lt;value_of_label_name&gt;<br>&nbsp;&nbsp;quantity -&gt; &lt;value_of_label_quantity&gt;<br>&nbsp;&nbsp;le  -&gt; &lt;value of the bucket&gt;<br>&nbsp;&nbsp;value -&gt; &lt;value_of_metric&gt;<br></p>
<p></p>
