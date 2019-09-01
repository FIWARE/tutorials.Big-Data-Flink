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

> :information_source: **Note:** If you want to clean up and start over again you can do so with the following command:
>
> ```console
> ./services stop
> ```

# MongoDB - Persisting Context Data into a Database

Persisting historic context data using MongoDB technology is relatively simple to configure since we are already using a MongoDB instance to hold data related to the Orion Context Broker and the IoT Agent. The MongoDB instance is listening on the standard `27017` port and the overall architecture can be seen below:

![](https://fiware.github.io/tutorials.Historic-Context-NIFI/img/mongo-draco-tutorial.png)

```yaml
mongo-db:
    image: mongo:3.6
    hostname: mongo-db
    container_name: db-mongo
    ports:
        - "27017:27017"
    networks:
        - default
    command: --bind_ip_all --smallfiles
```


To start the system with a **MongoDB** database only, run the following command:

```console
./services mongodb
```