[![FIWARE Banner](https://fiware.github.io/tutorials.Big-Data-Flink/img/fiware.png)](https://www.fiware.org/developers)
[![NGSI v2](https://img.shields.io/badge/NGSI-v2-5dc0cf.svg)](https://fiware-ges.github.io/orion/api/v2/stable/)
[![NGSI LD](https://img.shields.io/badge/NGSI-LD-d6604d.svg)](https://www.etsi.org/deliver/etsi_gs/CIM/001_099/009/01.08.01_60/gs_cim009v010801p.pdf)

[![FIWARE Core Context Management](https://nexus.lab.fiware.org/static/badges/chapters/core.svg)](https://github.com/FIWARE/catalogue/blob/master/core/README.md)
[![License: MIT](https://img.shields.io/github/license/fiware/tutorials.Big-Data-Flink.svg)](https://opensource.org/licenses/MIT)
[![Support badge](https://img.shields.io/badge/tag-fiware-orange.svg?logo=stackoverflow)](https://stackoverflow.com/questions/tagged/fiware)

This tutorial is an introduction to the [FIWARE Cosmos Orion Flink Connector](http://fiware-cosmos-flink.rtfd.io), which
facilitates Big Data analysis of context data, through an integration with [Apache Flink](https://flink.apache.org/),
one of the most popular Big Data platforms. Apache Flink is a framework and distributed processing engine for stateful
computations both over unbounded and bounded data streams. Flink has been designed to run in all common cluster
environments, perform computations at in-memory speed and at any scale.

The tutorial uses [cUrl](https://ec.haxx.se/) commands throughout, but is also available as
[Postman documentation](https://fiware.github.io/tutorials.Big-Data-Flink/)

# Start-Up

## NGSI-v2 Smart Supermarket


**NGSI-v2** offers JSON based interoperability used in individual Smart Systems. To run this tutorial with **NGSI-v2**, use the `NGSI-v2` branch.

```console
git clone https://github.com/FIWARE/tutorials.Big-Data-Flink.git
cd tutorials.Big-Data-Flink
git checkout NGSI-v2

./services create
./services start
```

| ![NGSI v2](https://img.shields.io/badge/NGSI-v2-5dc0cf.svg) | :books: [Documentation](https://github.com/FIWARE/tutorials.Big-Data-Flink/tree/NGSI-LD) | <img src="https://cdn.jsdelivr.net/npm/simple-icons@v3/icons/postman.svg" height="15" width="15"> [Postman Collection](https://fiware.github.io/tutorials.Big-Data-Flink/) |
| --- | --- | --- |

## NGSI-LD Smart Farm

**NGSI-LD** offers JSON-LD based interoperability used for Federations and Data Spaces. To run this tutorial with **NGSI-LD**, use the `NGSI-LD` branch.

```console
git clone https://github.com/FIWARE/tutorials.Big-Data-Flink.git
cd tutorials.Big-Data-Flink
git checkout NGSI-LD

./services create
./services start
```


| ![NGSI LD](https://img.shields.io/badge/NGSI-LD-d6604d.svg) | :books: [Documentation](https://github.com/FIWARE/tutorials.Big-Data-Flink/tree/NGSI-LD) | <img  src="https://cdn.jsdelivr.net/npm/simple-icons@v3/icons/postman.svg" height="15" width="15"> [Postman Collection](https://fiware.github.io/tutorials.Big-Data-Flink/ngsi-ld.html) |
| --- | --- | --- |


---

## License

[MIT](LICENSE) © 2020-2024 FIWARE Foundation e.V.
