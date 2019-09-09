[![FIWARE Banner](https://fiware.github.io/tutorials.Historic-Context-NIFI/img/fiware.png)](https://www.fiware.org/developers)

[![FIWARE Context processing, analysis and visualisation](https://nexus.lab.fiware.org/static/badges/chapters/processing.svg)](https://github.com/FIWARE/catalogue/blob/master/processing/README.md)
[![License: MIT](https://img.shields.io/github/license/fiware/tutorials.Historic-Context-NIFI.svg)](https://opensource.org/licenses/MIT)
[![NGSI v2](https://img.shields.io/badge/NGSI-v2-blue.svg)](https://fiware-ges.github.io/orion/api/v2/stable/)
[![Support badge](https://nexus.lab.fiware.org/repository/raw/public/badges/stackoverflow/fiware.svg)](https://stackoverflow.com/questions/tagged/fiware)
<br/> [![Documentation](https://img.shields.io/readthedocs/fiware-tutorials.svg)](https://fiware-tutorials.rtfd.io)

This tutorial is an introduction to the [FIWARE Cosmos Orion Flink Connector](http://fiware-cosmos-flink.rtfd.io), which enables easier Big Data analysis over context, integrated with one of the most popular BigData platforms: [Apache Flink](https://flink.apache.org/). Apache Flink is a framework and distributed processing engine for stateful computations over unbounded and bounded data streams. Flink has been designed to run in all common cluster environments, perform computations at in-memory speed and at any scale.

[//]: # The tutorial uses [cUrl](https://ec.haxx.se/) commands throughout, but is also available as [Postman documentation](https://fiware.github.io/tutorials.Historic-Context-NIFI/)

[//]: # [![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/9658043920d9be43914a)

<details>
<summary><strong>Details</strong></summary>

-   [Real-time Processing of Historic Context Information](#real-time-processing-of-historic-context-information-using-apache-flink)
-   [Architecture](#architecture)
-   [Prerequisites](#prerequisites)
    -   [Docker and Docker Compose](#docker-and-docker-compose)
    -   [Cygwin for Windows](#cygwin-for-windows)
-   [Start Up](#start-up)
-   [MongoDB - Persisting Context Data into a Database](#mongodb---persisting-context-data-into-a-database)
    -   [MongoDB - Database Server Configuration](#mongodb---database-server-configuration)
    -   [MongoDB - Draco Configuration](#mongodb---draco-configuration)
    -   [MongoDB - Start up](#mongodb---start-up)
        -   [Checking the Draco Service Health](#checking-the-draco-service-health)
        -   [Generating Context Data](#generating-context-data)
        -   [Subscribing to Context Changes](#subscribing-to-context-changes)
    -   [MongoDB - Reading Data from a database](#mongodb----reading-data-from-a-database)
        -   [Show Available Databases on the MongoDB server](#show-available-databases-on-the-mongodb-server)
        -   [Read Historical Context from the server](#read-historical-context-from-the-server)
-   [Next Steps](#next-steps)

</details>

# Real-time Processing of Historic Context Information using Apache Flink

> "Who controls the past controls the future: who controls the present controls the past."
>
> — George Orwell. "1984" (1949)


[FIWARE Cosmos](https://fiware-draco.readthedocs.io/en/latest/) is a Generic Enabler that allows for an easier Big Data analysis over context integrated with some of the most popular Big Data platforms, such as [Apache Flink](https://flink.apache.org/) and [Apache Spark](https://spark.apache.org/).

The [FIWARE Cosmos Orion Flink Connector](http://fiware-cosmos-flink.rtfd.io) is a software tool that enables a direct ingestion of the context data coming from the notifications sent by **Orion Context Broker**  to the Apache Flink processing engine. This allows to aggregate data in a time window in order to extract value from them in real-time.

#### Device Monitor

For the purpose of this tutorial, a series of dummy IoT devices have been created, which will be attached to the context broker. Details of the architecture and protocol used can be found in the [IoT Sensors tutorial](https://github.com/FIWARE/tutorials.IoT-Sensors). The state of each device can be seen on the UltraLight device monitor web page found at: `http://localhost:3000/device/monitor`

![FIWARE Monitor](https://fiware.github.io/tutorials.Historic-Context-NIFI/img/device-monitor.png)

# Architecture

This application builds on the components and dummy IoT devices created in [previous tutorials](https://github.com/FIWARE/tutorials.IoT-Agent/). It will make use of three FIWARE components - the [Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/), the [IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/), and the [Cosmos Orion Flink Connector](https://fiware-cosmos-flink.readthedocs.io/en/latest/) for connecting Orion to an Apache Flink cluster. Additional databases are now involved - both the Orion Context Broker and the IoT Agent rely on [MongoDB](https://www.mongodb.com/) technology to keep persistence of the information they hold 

Therefore the overall architecture will consist of the following elements:

-   Three **FIWARE Generic Enablers**:
    -   The FIWARE [Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/) which will receive requests
        using [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2)
    -   The FIWARE [IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/) which will
        receive northbound measurements from the dummy IoT devices in
        [Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual)
        format and convert them to [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2) requests for the
        context broker to alter the state of the context entities
    -   FIWARE [Cosmos Orion Flink Connector](https://fiware-cosmos-flink.readthedocs.io/en/latest/) which will subscribe to context changes and make operations on them in real-time
-   One **Database**:
    -   The underlying [MongoDB](https://www.mongodb.com/) database :
        -   Used by the **Orion Context Broker** to hold context data information such as data entities, subscriptions
            and registrations
        -   Used by the **IoT Agent** to hold device information such as device URLs and Keys
        -   Potentially used as a data sink to hold the processed context data.
-   Three **Context Providers**:
    -   The **Stock Management Frontend** is not used in this tutorial. It does the following:
        -   Display store information and allow users to interact with the dummy IoT devices
        -   Show which products can be bought at each store
        -   Allow users to "buy" products and reduce the stock count.
    -   A webserver acting as set of [dummy IoT devices](https://github.com/FIWARE/tutorials.IoT-Sensors) using the
        [Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual)
        protocol running over HTTP.
    -   The **Context Provider NGSI** proxy is not used in this tutorial. It does the following:
        -   receive requests using [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2)
        -   makes requests to publicly available data sources using their own APIs in a proprietary format
        -   returns context data back to the Orion Context Broker in
            [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2) format.

Since all interactions between the elements are initiated by HTTP requests, the entities can be containerized and run
from exposed ports.

The specific architecture of each section of the tutorial is discussed below.

# Prerequisites

## Docker and Docker Compose

To keep things simple all components will be run using [Docker](https://www.docker.com). **Docker** is a container
technology which allows to different components isolated into their respective environments.

-   To install Docker on Windows follow the instructions [here](https://docs.docker.com/docker-for-windows/)
-   To install Docker on Mac follow the instructions [here](https://docs.docker.com/docker-for-mac/)
-   To install Docker on Linux follow the instructions [here](https://docs.docker.com/install/)

**Docker Compose** is a tool for defining and running multi-container Docker applications. A series of
[YAML files](https://github.com/FIWARE/tutorials.Historic-Context-NIFI/tree/master/docker-compose) are used configure
the required services for the application. This means all container services can be brought up in a single command.
Docker Compose is installed by default as part of Docker for Windows and Docker for Mac, however Linux users will need
to follow the instructions found [here](https://docs.docker.com/compose/install/)

You can check your current **Docker** and **Docker Compose** versions using the following commands:

```console
docker-compose -v
docker version
```

Please ensure that you are using Docker version 18.03 or higher and Docker Compose 1.21 or higher and upgrade if
necessary.

## Cygwin for Windows

We will start up our services using a simple Bash script. Windows users should download [cygwin](http://www.cygwin.com/)
to provide a command-line functionality similar to a Linux distribution on Windows.

# Start Up

Before you start you should ensure that you have obtained or built the necessary Docker images locally. Please clone the
repository and create the necessary images by running the commands as shown:

```console
git clone https://github.com/sonsoleslp/fiware-cosmos-orion-flink-connector-tutorial.git
./services create
```

Thereafter, all services can be initialized from the command-line by running the
[services](https://github.com/FIWARE/tutorials.Historic-Context-NIFI/blob/master/services) Bash script provided within
the repository:

```console
./services <command>
```

Where `<command>` will vary depending upon the databases we wish to activate. This command will also import seed data
from the previous tutorials and provision the dummy IoT sensors on startup.

To start the system, run the following command:

```console
./services start
```

> :information_source: **Note:** If you want to clean up and start over again you can do so with the following command:
>
> ```console
> ./services stop
> ```




### Generating Context Data

For the purpose of this tutorial, we must be monitoring a system where the context is periodically being updated. The
dummy IoT Sensors can be used to do this. Open the device monitor page at `http://localhost:3000/device/monitor` and
unlock a **Smart Door** and switch on a **Smart Lamp**. This can be done by selecting an appropriate the command from
the drop down list and pressing the `send` button. The stream of measurements coming from the devices can then be seen
on the same page:

![](https://fiware.github.io/tutorials.Historic-Context-NIFI/img/door-open.gif)

### Subscribing to context changes

Once a dynamic context system is up and running, we need to inform **Draco** of changes in context.

This is done by making a POST request to the `/v2/subscription` endpoint of the Orion Context Broker.

-   The `fiware-service` and `fiware-servicepath` headers are used to filter the subscription to only listen to
    measurements from the attached IoT Sensors, since they had been provisioned using these settings
-   The `idPattern` in the request body ensures that Draco will be informed of all context data changes.
-   The notification `url` must match the configured `Base Path and Listening port` of the Draco Listen HTTP Processor
-   The `throttling` value defines the rate that changes are sampled.

Do not forget to change $MY_IP to your machine's IP Address (must be accesible from the docker container):

#### :one: Request:

```console
curl -iX POST \
  'http://localhost:1026/v2/subscriptions' \
  -H 'Content-Type: application/json' \
  -H 'fiware-service: openiot' \
  -H 'fiware-servicepath: /' \
  -d '{
  "description": "Notify Flink of all context changes",
  "subject": {
    "entities": [
      {
        "idPattern": ".*"
      }
    ]
  },
  "notification": {
    "http": {
      "url": "http://$MY_IP:9001/v2/notify"
    }
  },
  "throttling": 5
}'
```

As you can see, the database used to persist context data has no impact on the details of the subscription. It is the
same for each database. The response will be **201 - Created**

If a subscription has been created, you can check to see if it is firing by making a GET request to the
`/v2/subscriptions` endpoint.

#### :two: Request:

```console
curl -X GET \
  'http://localhost:1026/v2/subscriptions/' \
  -H 'fiware-service: openiot' \
  -H 'fiware-servicepath: /'
```

#### Response:

```json
[
  {
    "id": "5d76059d14eda92b0686f255",
    "description": "Notify Flink of all context changes",
    "status": "active",
    "subject": {
      "entities": [
        {
          "idPattern": ".*"
        }
      ],
      "condition": {
        "attrs": []
      }
    },
    "notification": {
      "timesSent": 362,
      "lastNotification": "2019-09-09T09:36:33.00Z",
      "attrs": [],
      "attrsFormat": "normalized",
      "http": {
        "url": "http://$MY_IP:9001/v2/notify"
      },
      "lastSuccess": "2019-09-09T09:36:33.00Z",
      "lastSuccessCode": 200
    },
    "throttling": 5
  }
]

```

Within the `notification` section of the response, you can see several additional `attributes` which describe the health
of the subscription

If the criteria of the subscription have been met, `timesSent` should be greater than `0`. A zero value would indicate
that the `subject` of the subscription is incorrect or the subscription has created with the wrong `fiware-service-path`
or `fiware-service` header

The `lastNotification` should be a recent timestamp - if this is not the case, then the devices are not regularly
sending data. Remember to unlock the **Smart Door** and switch on the **Smart Lamp**

The `lastSuccess` should match the `lastNotification` date - if this is not the case then **Cosmos** is not receiving the
subscription properly. Check that the hostname and port are correct.

Finally, check that the `status` of the subscription is `active` - an expired subscription will not fire.

# Example 1: Receiving data and performing operations

The first example makes use of the OrionSource in order to receive notifications from the Orion Context Broker. Specifically, the example receives a notification every time that a IoT device changes his status, and calculates the sum of each type of device.