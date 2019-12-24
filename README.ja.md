[![FIWARE Banner](https://fiware.github.io/tutorials.Big-Data-Analysis/img/fiware.png)](https://www.fiware.org/developers)

[![FIWARE Context processing, analysis and visualisation](https://nexus.lab.fiware.org/static/badges/chapters/processing.svg)](https://github.com/FIWARE/catalogue/blob/master/processing/README.md)
[![License: MIT](https://img.shields.io/github/license/fiware/tutorials.Big-Data-Analysis.svg)](https://opensource.org/licenses/MIT)
[![NGSI v2](https://img.shields.io/badge/NGSI-v2-blue.svg)](https://fiware-ges.github.io/orion/api/v2/stable/)
[![Support badge](https://nexus.lab.fiware.org/repository/raw/public/badges/stackoverflow/fiware.svg)](https://stackoverflow.com/questions/tagged/fiware)
<br/> [![Documentation](https://img.shields.io/readthedocs/fiware-tutorials.svg)](https://fiware-tutorials.rtfd.io)

このチュートリアルは、[FIWARE Cosmos Orion Flink Connector](http://fiware-cosmos-flink.rtfd.io) の概要であり、最も
一般的な BigData プラットフォームの1つである、[Apache Flink](https://flink.apache.org/) と統合された、コンテキスト
を介した簡単なビッグデータ分析を可能にします。Apache Flink は、無制限および有界のデータストリーム上でステートフルな
計算を行うためのフレームワークおよび分散処理エンジンです。Flink は、すべての一般的なクラスタ環境で実行され、メモリ
内の速度と任意の規模で計算を実行するように設計されています。

チュートリアルでは [cUrl](https://ec.haxx.se/) コマンドを使用しますが、
[Postman documentation](https://fiware.github.io/tutorials.Big-Data-Analysis/) としても利用可能です。

[![Run in Postman](https://run.pstmn.io/button.svg)](https://www.getpostman.com/collections/64eda5ebb4b337c8784f)

## コンテンツ

<details>

<summary><strong>詳細</strong></summary>

-   [Apache Flink を使用した履歴コンテキスト情報のリアルタイム処理](#real-time-processing-of-historic-context-information-using-apache-flink)
    -   [Device Monitor](#device-monitor)
-   [アーキテクチャ](#architecture)
-   [前提条件](#prerequisites)
    -   [Docker および Docker Compose](#docker-and-docker-compose)
    -   [Maven](#maven)
    -   [IntelliJ (optional)](#intellij-optional)
    -   [Cygwin for Windows](#cygwin-for-windows)
-   [起動](#start-up)
    -   [コンテキスト・データの生成](#generating-context-data)
    -   [サンプルをローカルで実行](#running-examples-locally)
-   [例](#example)
    -   [例1 : データの受信と操作の実行](#example-1-receiving-data-and-performing-operations)
        -   [コンテキスト変更のサブスクライブ](#subscribing-to-context-changes)
            -   [:one: リクエスト:](#one-request)
            -   [:two: リクエスト:](#two-request)
            -   [レスポンス:](#response)
    -   [例2 : データの受信、操作の実行、Context Broker への書き戻し](#example-2--receiving-data-performing-operations-and-writing-back-to-the-context-broker)
        -   [ランプをオンにする](#switching-on-a-lamp)
        -   [シナリオのセットアップ](#setting-up-the-scenario)
    -   [例3 : コードをパッケージ化し、Flink Job Manager に送信](#example-3-packaging-the-code-and-submitting-it-to-the-flink-job-manager)
        -   [通知のサブスクライブ](#subscribing-to-notifications)
        -   [コードの変更](#changing-the-code)
        -   [コードのパッケージ化](#packaging-the-code)
        -   [ジョブを送信](#submitting-the-job)

</details>

<a name="real-time-processing-of-historic-context-information-using-apache-flink"></a>

# Apache Flinkを使用した履歴コンテキスト情報のリアルタイム処理

> "Who controls the past controls the future: who controls the present controls the past."
>
> — George Orwell. "1984" (1949)

[FIWARE Cosmos](https://fiware-cosmos-flink.readthedocs.io/en/latest/) は、一般的なイネーブラーであり、
[Apache Flink](https://flink.apache.org/) や [Apache Spark](https://spark.apache.org/) のような、最も人気のある
ビッグデータ・プラットフォームの一部と統合され、コンテキストでのビッグデータ分析を容易にします。

[FIWARE Cosmos Orion Flink Connector](http://fiware-cosmos-flink.rtfd.io) は、**Orion Context Broker** によって
送信された通知からのコンテキスト・データを、Apache Flink 処理エンジンに直接取り込むことができるソフトウェア・
ツールです。これにより、時間枠内のデータを集計して、リアルタイムでデータから値を抽出できます。

<a name="device-monitor"></a>

## Device Monitor

このチュートリアルの目的のために、一連のダミー IoT デバイスが作成され、Context Broker に接続されます。使用される
アーキテクチャとプロトコルの詳細は、[IoT Sensors tutorial](https://github.com/FIWARE/tutorials.IoT-Sensors) に
記載されています。各デバイスの状態は、`http://localhost:3000/device/monitor` にある UltraLight device monitor の
Web ページで確認できます。

![FIWARE Monitor](https://fiware.github.io/tutorials.Big-Data-Analysis/img/device-monitor.png)

<a name="architecture"></a>

# アーキテクチャ

このアプリケーションは、[以前のチュートリアル](https://github.com/FIWARE/tutorials.IoT-Agent/) で作成された
コンポーネントとダミー IoT デバイス上に構築されます。3つの FIWARE コンポーネント -
[Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/),
[IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/), と
[Cosmos Orion Flink Connector](https://fiware-cosmos-flink.readthedocs.io/en/latest/) は、Orion を Apache Flink
クラスタに接続します。これで、データベースが追加されました。Orion Context Broker と IoT Agent は、保持する情報の
永続性を維持するために [MongoDB](https://www.mongodb.com/) テクノロジを利用しています。

したがって、全体的なアーキテクチャは次の要素で構成されます :

-   3つの **FIWARE Generic Enablers** :
    -   FIWARE [Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/)は、
        [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2) を使用してリクエストを受信します
    -   FIWARE [IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/) は、ダミー IoT
        デバイスから Ultralight 2.0 形式のノースバウンド測定値を受信し、Context Broker の
        [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2) リクエストに変換して、コンテキスト・
        エンティティの状態を変更します
    -   FIWARE [Cosmos Orion Flink Connector](https://fiware-cosmos-flink.readthedocs.io/en/latest/) は、
        コンテキストの変更をサブスクライブし、リアルタイムで操作を行います
-   1つの**データベース** :
    -   [MongoDB](https://www.mongodb.com/) データベース :
        -   **Orion Context Broker** がデータのエンティティ、サブスクリプション、レジストレーションなどの
            コンテキスト・データの情報を保持するために使用します
        -   **IoT Agent** がデバイスの URL やキーなどのデバイス情報を保持するために使用します
-   3つの**コンテキスト・プロバイダ** :
    -   **在庫管理フロントエンド** は、このチュートリアルでは使用しません。次のことを行います :
        -   ストア情報を表示し、ユーザがダミー IoT デバイスと対話できるようにします
        -   各ストアで購入できる製品を表示します
        -   ユーザが製品を "購入" して在庫数を減らすことを許可します
        -   HTTP 上で実行される
            [Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual)
            を使用する、[ダミー IoT デバイス](https://github.com/FIWARE/tutorials.IoT-Sensors) のセットとして
            機能する Webサーバ
    -   **Context Provider NGSI** プロキシは、このチュートリアルでは使用しません。次のことを行います :
        -   [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2) を使用してリクエストを受信します
        -   独自形式の独自 API を使用して、公開されているデータソースへのリクエストを行います
        -   コンテキスト・データを[NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2) 形式で
            Orion Context Broker に返します

要素間の相互作用はすべて HTTP リクエストによって開始されるため、エンティティはコンテナ化され、公開されたポートから
実行できます。

チュートリアルの各セクションの特定のアーキテクチャについては、以下で説明します。

<a name="prerequisites"></a>

# 前提条件

<a name="docker-and-docker-compose"></a>

## Docker および Docker Compose

物事を単純にするために、すべてのコンポーネントは [Docker](https://www.docker.com) を使用して実行されます。
**Docker** は、さまざまなコンポーネントをそれぞれの環境に分離できるようにするコンテナ・テクノロジーです。

-   Windows に Docker をインストールするには、[こちら](https://docs.docker.com/docker-for-windows/)の指示に従って
    ください
-   Mac に Docker をインストールするには、[こちら](https://docs.docker.com/docker-for-mac/)の指示に従ってください
-   Linux に Docker をインストールするには、[こちら](https://docs.docker.com/install/)の指示に従ってください

**Docker Compose** は、マルチ・コンテナ Docker アプリケーションを定義および実行するためのツールです。一連の
[YAML files](https://github.com/FIWARE/tutorials.Big-Data-Analysis/tree/master/docker-compose) は、アプリケーション
に必要なサービスを構成するために使用されます。これは、すべてのコンテナ・サービスを単一のコマンドで起動できることを
意味します。Docker Compose は、デフォルトで Docker for Windows および Docker for Mac の一部としてインストール
されますが、Linux ユーザは[こちら](https://docs.docker.com/compose/install/)にある指示に従う必要があります。

次のコマンドを使用して、現在の **Docker** および **Docker Compose** バージョンを確認できます :

```console
docker-compose -v
docker version
```

Docker バージョン18.03 以降および Docker Compose 1.21 以降を使用していることを確認し、必要に応じてアップグレード
してください。

<a name="maven"></a>

## Maven

[Apache Maven](https://maven.apache.org/download.cgi) は、ソフトウェア・プロジェクト管理ツールです。プロジェクト・
オブジェクト・モデル (POM) の概念に基づいて、Maven は情報の中心部分からプロジェクトのビルド、レポート、および
ドキュメントを管理できます。Maven を使用して、依存関係を定義およびダウンロードし、コードをビルドして JAR ファイルに
パッケージ化します。

<a name="intellij-optional"></a>

## IntelliJ (optional)

[IntelliJ](https://www.jetbrains.com/idea/) は、Scala プログラムの開発を容易にする IDE です。コードを作成して実行
するために使用します。

<a name="cygwin-for-windows"></a>

## Cygwin for Windows

簡単な Bash スクリプトを使用してサービスを開始します。Windows ユーザは、[cygwin](http://www.cygwin.com/) を
ダウンロードして、Windows 上の Linux ディストリビューションに類似したコマンドライン機能を提供する必要があります。

<a name="start-up"></a>

# 起動

開始する前に、必要な Docker イメージをローカルで取得または構築したことを確認する必要があります。以下に示すコマンド
を実行して、リポジトリを複製し、必要なイメージを作成してください。いくつかのコマンドを特権ユーザとして実行する
必要がある場合があることに注意してください :

```console
git clone https://github.com/FIWARE/tutorials.Big-Data-Analysis.git
cd tutorials.Big-Data-Analysis
./services create
```

このコマンドは、以前のチュートリアルからシードデータをインポートし、起動時にダミー IoT センサをプロビジョニング
します。

システムを起動するには、次のコマンドを実行します :

```console
./services start
```

> :information_source: **注 :** クリーンアップしてやり直す場合は、次のコマンドを使用します :
>
> ```
> ./services stop
> ```

次に、Orion Flink Connector を使用するには、Maven を使用して JAR をインストールする必要があります :

```console
cd job
mvn install:install-file \
  -Dfile=./orion.flink.connector-1.2.1.jar \
  -DgroupId=org.fiware.cosmos \
  -DartifactId=orion.flink.connector \
  -Dversion=1.2.1 \
  -Dpackaging=jar
```

<a name="generating-context-data"></a>

## コンテキスト・データの生成

このチュートリアルでは、コンテキストが定期的に更新されるシステムを監視する必要があります。これを行うには、ダミー
IoT センサを使用できます。`http://localhost:3000/device/monitor` の Device monitor のページを開き、**Smart Door**
のロックを解除して、**Smart Lamp** をオンにします。これは、ドロップ・ダウン・リストから適切なコマンドを選択し、
`send` ボタンを押すことで実行できます。デバイスからの測定値のストリームは、同じページで見ることができます :

![](https://fiware.github.io/tutorials.Big-Data-Analysis/img/door-open.gif)

<a name="running-examples-locally"></a>

## サンプルをローカルで実行

ローカルで実行するには、[IntelliJ](https://www.jetbrains.com/idea/download) をダウンロードし、
[Maven](https://www.jetbrains.com/help/idea/maven-support.html#maven_import_project_start) を使用して
プロジェクトの `job` ディレクトリを開く必要があります。JDK 1.8 を使用してください。

<a name="example"></a>

# 例

<a name="example-1-receiving-data-and-performing-operations"></a>

## 例1 : データの受信と操作の実行

最初の例は、Orion Context Broker から通知を受信するために OrionSource を利用しています。具体的には、この例では、
各タイプのデバイスが1分間に送信する通知の数をカウントします。例1のコードは、
`job/src/main/scala/org/fiware/cosmos/orion/flink/connector/tutorial/example1/Example1.scala` にあります。

```scala
package org.fiware.cosmos.orion.flink.connector.tutorial.example1

import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}

import org.apache.flink.streaming.api.windowing.time.Time

import org.fiware.cosmos.orion.flink.connector.{OrionSource}


object Example1{

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = env.addSource(new OrionSource(9001))

    // Process event stream
    val processedDataStream = eventStream
    .flatMap(event => event.entities)
    .map(entity => new Sensor(entity.`type`,1))
    .keyBy("device")
    .timeWindow(Time.seconds(60))
    .sum(1)

    // print the results with a single thread, rather than in parallel
    processedDataStream.print().setParallelism(1)
    env.execute("Socket Window NgsiEvent")
  }
  case class Sensor(device: String, sum: Int)
}
```

プログラムの最初の行は、コネクタを含む必要な依存関係をインポートすることを目的としています。その後、最初のステップ
は、コネクタによって提供されるクラスを使用して Orion Source のインスタンスを作成し、それを Flink によって提供される
環境に追加することです。

```scala
val eventStream = env.addSource(new OrionSource(9001))
```

`OrionSource` はパラメータとしてポート番号を受け入れます。コネクタは、このポートを介して Orion からのデータを
リッスンします。これらのデータは、`NgsiEvent` オブジェクトの　`DataStream` の形式になります。

[connector docs](https://github.com/ging/fiware-cosmos-orion-flink-connector/blob/master/README.md#orionsource)
でこのオブジェクトの詳細を確認できます。

この例では、処理の最初のステップはエンティティのフラット・マッピングです。この操作は、一定期間内に受信したすべての
NGSI イベントのエンティティ・オブジェクトをまとめるために実行されます。

```scala
val processedDataStream = eventStream

.flatMap(event => event.entities)
```

すべてのエンティティをまとめたら、`map` でエンティティを反復処理し、目的の属性を抽出します。この場合、
センサ・タイプ (ドア, モーション, ベル または ランプ) に関心があります。

```scala
// ...

.map(entity => new Sensor(entity.`type`,1))
```

各反復で、必要なプロパティを持つカスタム・オブジェクトを作成します。センサ・タイプと各通知の増分です。
このために、次のようにケースクラスを定義できます :

```scala
case class Sensor(device: String, sum: Int)
```

これで、作成したオブジェクトをデバイスのタイプ別にグループ化し、それらに対して操作を実行できます :

```scala
// ...

.keyBy("device")
```

次のようなカスタム処理ウィンドウを提供できます :

```scala
// ...

.timeWindow(Time.seconds(60))
```

そして、上記の時間間隔で実行する操作を指定します :

```scala
// ...

.sum(1)
```

処理後、結果をコンソールに印刷できます :

```scala
processedDataStream.print().setParallelism(1)
```

または、選択したシンクを使用して永続化することもできます。これで、IntelliJ の再生ボタンを押すことでコードを
実行できます。

<a name="subscribing-to-context-changes"></a>

### コンテキスト変更のサブスクライブ

動的コンテキスト・システムが起動して実行されると (例1を実行)、**Flink** にコンテキストの変更を通知する必要が
あります。

これは、Orion Context Broker の `/v2/subscription` エンドポイントに POST リクエストを行うことで実行できます。

-   これらの設定を使用してプロビジョニングされているため、`fiware-service` および `fiware-servicepath` ヘッダを
    使用してサブスクリプションをフィルタリングし、接続された IoT センサからの測定値のみをリッスンします

-   通知の `url` は、Flink プログラムがリッスンしているものと一致する必要があります。docker0 ネットワーク内の
    マシンの IP アドレスを  ${MY_IP} に置き換えます (Docker コンテナからアクセスできる必要があります)。
    次のようにこの IP を取得できます (sudo が必要な場合があります) :

```console
docker network inspect bridge --format='{{(index .IPAM.Config 0).Gateway}}'
```

-   `throttling` 値は、変更がサンプリングされるレートを定義します

<a name="one-request"></a>

#### :one: リクエスト :

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
    "url": "http://${MY_IP}:9001"
    }
  },
  "throttling": 5
}'
```

レスポンスは `**201 - Created**` になります

サブスクリプションが作成されている場合、`/v2/subscriptions` エンドポイントに対して GET リクエストを行うことで、
サブスクリプションが起動しているかどうかを確認できます。

<a name="two-request"></a>

#### :two: リクエスト :

```console
curl -X GET \
'http://localhost:1026/v2/subscriptions/' \
-H 'fiware-service: openiot' \
-H 'fiware-servicepath: /'
```

<a name="response"></a>

#### レスポンス :

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
                "url": "http://${MY_IP}:9001"
            },
            "lastSuccess": "2019-09-09T09:36:33.00Z",
            "lastSuccessCode": 200
        },
        "throttling": 5
    }
]
```

レスポンスの `notification` セクション内で、サブスクリプションの正常性を説明するいくつかの追加の  `attributes`
を確認できます

サブスクリプションの基準が満たされている場合、`timesSent` は `0` より大きくなければなりません。ゼロの値は、
サブスクリプションの `subject` が正しくないか、サブスクリプションが間違った `fiware-service-path` または
`fiware-service` ヘッダで作成されたことを示します。

`lastNotification` は最新のタイムスタンプである必要があります。そうでない場合、デバイスは定期的にデータを
送信していません。**Smart Door** のロックを解除し、**Smart Lamp** をオンにしてください。

`lastSuccess` は `lastNotification` の日付と一致する必要があります-そうでない場合、**Cosmos** はサブスクリプション
を適切に受信していません。ホスト名とポートが正しいことを確認してください。

最後に、サブスクリプションの `status` が `active` であることを確認します。有効期限が切れたサブスクリプションは
実行されません。

サブスクリプションを作成すると、IntelliJ コンソールの出力は次のようになります :

```
Sensor(Bell,3)
Sensor(Door,4)
Sensor(Lamp,7)
Sensor(Motion,6)
```
<a name="example-2--receiving-data-performing-operations-and-writing-back-to-the-context-broker"></a>

## 例2 : データの受信、操作の実行、Context Broker への書き戻し

2番目の例では、モーション・センサが動きを検出するとランプをオンにします。

<a name="switching-on-a-lamp"></a>

### ランプをオンにする

次に Example2 のコードを見てみましょう :

```scala
package org.fiware.cosmos.orion.flink.connector.tutorial.example2


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector._


object Example2{
  final val CONTENT_TYPE = ContentType.Plain
  final val METHOD = HTTPMethod.POST
  final val CONTENT = "{\n  \"on\": {\n      \"type\" : \"command\",\n      \"value\" : \"\"\n  }\n}"
  final val HEADERS = Map("fiware-service" -> "openiot","fiware-servicepath" -> "/","Accept" -> "*/*")

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
  // Create Orion Source. Receive notifications on port 9001
  val eventStream = env.addSource(new OrionSource(9001))

    // Process event stream
  val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .filter(entity=>(entity.attrs("count").value == "1"))
      .map(entity => new Sensor(entity.id))
      .keyBy("id")
      .timeWindow(Time.seconds(5),Time.seconds(2))
      .min("id")

    // print the results with a single thread, rather than in parallel
  processedDataStream.print().setParallelism(1)

    val sinkStream = processedDataStream.map(node => {
      new OrionSinkObject("urn:ngsi-ld:Lamp"+ node.id.takeRight(3)+ "@on","http://${IP}:3001/iot/lamp"+ node.id.takeRight(3),CONTENT_TYPE,METHOD)
    })
    OrionSink.addSink(sinkStream)
    env.execute("Socket Window NgsiEvent")
  }

  case class Sensor(id: String)
}
```

ご覧のとおり、前の例と似ています。主な違いは、処理されたデータを **`OrionSink`** を介して Context Broker
に書き戻すことです。

```scala
val sinkStream = processedDataStream.map(node => {
      new OrionSinkObject(CONTENT, "http://localhost:1026/v2/entities/Lamp:"+node.id.takeRight(3)+"/attrs", CONTENT_TYPE,         METHOD, HEADERS)
    })

OrionSink.addSink(sinkStream)
```

**`OrionSinkObject`** の引数は次のとおりです :

-   **Message**: `"{\n \"on\": {\n \"type\" : \"command\",\n \"value\" : \"\"\n }\n}"`. 'on' コマンドを送信します
-   **URL**: `"http://localhost:1026/v2/entities/Lamp:"+node.id.takeRight(3)+"/attrs"`. TakeRight(3) は、
    部屋の数を取得します。例えば '001'
-   **Content Type**: `ContentType.Plain`.
-   **HTTP Method**: `HTTPMethod.POST`.
-   **Headers**: `Map("fiware-service" -> "openiot","fiware-servicepath" -> "/","Accept" -> "*/*")`.
    オプションのパラメータ。HTTP リクエストに必要なヘッダを追加します

<a name="setting-up-the-scenario"></a>

### シナリオのセットアップ

最初に、前に作成したサブスクリプションを削除する必要があります :

```console
curl -X DELETE   'http://localhost:1026/v2/subscriptions/$subscriptionId'   -H 'fiware-service: openiot'   -H 'fiware-servicepath: /'
```

`/v2/subscriptions` エンドポイントに対して GET リクエストを実行することにより、サブスクリプションの ID
を取得できます。

```console
curl -X GET   'http://localhost:1026/v2/subscriptions/'   -H 'fiware-service: openiot'   -H 'fiware-servicepath: /'
```

次に、モーション・センサが動きを検出したときにのみ通知をトリガーする他のサブスクリプションを作成します。
前述のように、$MY_IP を docker0 ネットワーク内のマシンの IP アドレスに変更することを忘れないでください。

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
        "idPattern": "Motion.*"
      }
    ]
  },
  "notification": {
    "http": {
      "url": "http://${MY_IP}:9001/v2/notify"
    }
  },
  "throttling": 5
}'
```

ドアを開けると、ランプが点灯します。

<a name="example-3-packaging-the-code-and-submitting-it-to-the-flink-job-manager"></a>

## 例3 : コードをパッケージ化し、Flink Job Manager に送信

前の例では、IntelliJ などの IDE からコネクタを起動して実行する方法を見てきました。実際のシナリオでは、コードを
パッケージ化し、Flink クラスタに送信して、操作を並行して実行することができます。Flink Dashoard はポート 8081
でリッスンしています :

![Screenshot](https://fiware.github.io/tutorials.Big-Data-Analysis//img/Tutorial%20FIWARE%20Flink.png)

<a name="subscribing-to-notifications"></a>

### 通知のサブスクライブ

最初に、次のように Flink ノードを指すようにサブスクリプションの通知 URL を変更する必要があります :

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
        "idPattern": "Motion.*"
      }
    ]
  },
  "notification": {
    "http": {
      "url": "http://taskmanager:9001/notify"
    }
  },
  "throttling": 5
}'
```

<a name="changing-the-code"></a>

### コードの変更

orion コンテナのホスト名の例2で localhost を変更する必要があります :

-   例2

```scala
      new OrionSinkObject(CONTENT, "http://localhost:1026/v2/entities/Lamp:"+node.id.takeRight(3)+"/attrs", CONTENT_TYPE, METHOD, HEADERS)
```

-   例3

```scala
      new OrionSinkObject(CONTENT, "http://orion:1026/v2/entities/Lamp:"+node.id.takeRight(3)+"/attrs", CONTENT_TYPE, METHOD, HEADERS)
```

<a name="packaging-the-code"></a>

### コードのパッケージ化

例の JAR パッケージをビルドしましょう。その中に、コネクタなど、使用したすべての依存関係を含める必要がありますが、
環境によって提供される依存関係の一部 (Flink, Scala...) は除外します。これは、`add-dependencies-for-IDEA`
プロファイルをチェックせずに `maven package` コマンドで実行できます。これにより、
`target/orion.flink.connector.tutorial-1.0-SNAPSHOT.jar` の下に JAR ファイルが構築されます。

<a name="submitting-the-job"></a>

### ジョブを送信

例3のコードを、デプロイした Flink クラスタに送信しましょう。これを行うには、ブラウザで
([http://localhost:8081](http://localhost:8081)) にある Flink GUI を開き、左側のメニューで  **Submit new Job**
セクションを選択します。**Add New** ボタンをクリックして、JAR をアップロードします。アップロードしたら、
**Uploaded JARs** リストから選択し、実行するクラスを指定します :

```scala
org.fiware.cosmos.orion.flink.connector.tutorial.example2.Example2
```

![Screenshot](https://fiware.github.io/tutorials.Big-Data-Analysis//img/submit-flink.png)

このフィールドに入力し、**Submit** ボタンをクリックすると、出力ジョブが実行されていることがわかります。
これでドアを開けて、ランプが点灯するのを見ることができます。
