#WSO2 Gateway (WSO2 GW)
WSO2 Gateway (WSO2 GW) is an ultra high performance, lightweight and configuration-driven message gateway based on
standard gateway pattern. It aims to encapsulate messaging between source and target systems that are built with
disparate technologies, protocols, and standards. While it includes messaging between two systems, message mediation
can be controlled by configuring WSO2 GW’s mediation logic.


##Key Features

* Ultra high performance and low latency  HTTP/S messaging.

* Supports thousands of concurrent connections/clients.

* Header-based routing using Apache Camel as the message mediation engine.

* Defines REST services/APIs using Camel REST DSL.

* Lightweight and stateless service orchestration.

* Load balancing and failover messaging.

* Error handling support. 

##Getting Started

By configuring the [camel-context.xml](https://github.com/wso2/product-gw/blob/master/product/carbon-home/conf/camel/camel-context.xml) (which can be found in `$CARBON_HOME/conf/camel/camel-context.xml`)
we can achieve camel routing.

####Sample camel configuration for Header-based routing
```
    <route id="http-routes">
        <from uri="wso2-gw:/default"/>
        <choice>
            <when>
                <simple>${header.routeId} regex 'r1'</simple>
                <to uri="wso2-gw:http://localhost:9000/services/SimpleStockQuoteService"/>
            </when>
            <when>
                <simple>${header.routeId} regex 'r2'</simple>
                <to uri="wso2-gw:http://localhost:9002/service/SimpleStockQuoteService"/>
            </when>
            <otherwise>
                <to uri="wso2-gw:http://localhost:9004/SimpleStockQuoteService"/>
            </otherwise>
        </choice>
    </route>
```

 Sample request to route to localhost:9000
 curl  http://localhost:9090/default -H __"routeId:r1"__

 If we don't have any routeId header the request will be routed to the localhost:9004 (i.e to otherwise)

####Sampel REST configuration

Following is a sample rest interface definition
```
    <rest path="/gw">
        <get uri="/news">
            <to uri="direct:getNews"/>
        </get>
        <get uri="/news/{id}">
            <to uri="direct:getNewsById"/>
        </get>
    </rest>
```

and follwoing is the corresponding routes
```
    <route>
        <from uri="direct:getNews"/>
        <to uri="wso2-gw:http://jsonplaceholder.typicode.com/posts"/>
    </route>
    <route>
        <from uri="direct:getNewsById"/>
        <toD uri="wso2-gw:http://jsonplaceholder.typicode.com/posts/${header.id}"/>
    </route>
```

when we invoke the request `http://localhost:9090/gw/news`
it will be routed to `http://jsonplaceholder.typicode.com/posts`

similarly `http://localhost:9090/gw/news/24` will be routed to `http://jsonplaceholder.typicode.com/posts/24`

####Route Hot Deployment
By default we are keeping all the route configurations in the `camel-context.xml`. If we want, we can hot deploy the routes (i.e we can add routes without restarting the server) by adding custom routes configuration files. Custom routes can be added by following the below steps.

* Multiple custom route configuration files can be placed with the following structure.

```
<routes xmlns="http://camel.apache.org/schema/spring">

    <route id="bar">
        <from uri="wso2-gw:/barroute"/>
        <to uri="wso2-gw:http://localhost:8080"/>
    </route>

    <route id="foo">
        <from uri="wso2-gw:/fooroute"/>
        <to uri="wso2-gw:http://localhost:9000/services/SimpleStockQuoteService"/>
    </route>

</routes>
```
* Copy the route configuration file to `$CARBON_HOME/deployment/camel`
* Routes added to the existing camel-context.xml configuration file, will not be hot deployed
* You may have custom route configuration files already in the `$CARBON_HOME/deployment/camel` directory, before the server starts. These custom routes will be loaded to the context when the server starts
* If you add a custom route and it is an existing one, the latter one will override the former one
* Do not add two routes with same id in the same configuration file
 

##Write Your Own Custom Engine 

This Product GW has been designed in such a way that any custom engine can be plugged in easily.

A simple engine can be found here (https://github.com/Venkat2811/MockEngine)
