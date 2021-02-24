# Big Data Analysis (Flink)[<img src="https://img.shields.io/badge/NGSI-LD-d6604d.svg" width="90"  align="left" />]("https://www.etsi.org/deliver/etsi_gs/CIM/001_099/009/01.03.01_60/gs_cim009v010301p.pdf)[<img src="https://fiware.github.io/tutorials.Big-Data-Flink/img/fiware.png" align="left" width="162">](https://www.fiware.org/)<br/>

[![FIWARE Banner](https://fiware.github.io/tutorials.Big-Data-Flink/img/fiware.png)](https://www.fiware.org/developers)
[![NGSI LD](https://img.shields.io/badge/NGSI-LD-d6604d.svg)](https://www.etsi.org/deliver/etsi_gs/CIM/001_099/009/01.03.01_60/gs_cim009v010301p.pdf)

[![FIWARE Core Context Management](https://nexus.lab.fiware.org/static/badges/chapters/core.svg)](https://github.com/FIWARE/catalogue/blob/master/core/README.md)
[![License: MIT](https://img.shields.io/github/license/fiware/tutorials.Big-Data-Flink.svg)](https://opensource.org/licenses/MIT)
[![Support badge](https://img.shields.io/badge/tag-fiware-orange.svg?logo=stackoverflow)](https://stackoverflow.com/questions/tagged/fiware)
 <br/>
[![Documentation](https://img.shields.io/readthedocs/fiware-tutorials.svg)](https://fiware-tutorials.rtfd.io)

このチュートリアルは [FIWARE Cosmos Orion Flink Connector](http://fiware-cosmos-flink.rtfd.io) の紹介です。これは、最も
人気のあるビッグデータ・プラットフォームの1つである [Apache Flink](https://flink.apache.org/) との統合により、コンテキスト
・データのビッグデータ分析を容易にします。Apache Flink は、無制限および有界のデータ・ストリーム上でステートフルな計算を
行うためのフレームワークおよび分散処理エンジンです。Flink は、すべての一般的なクラスタ環境で実行され、メモリ内の速度と
任意の規模で計算を実行するように設計されています。

チュートリアルでは [cUrl](https://ec.haxx.se/) コマンドを使用しますが、
[Postman ドキュメント](https://fiware.github.io/tutorials.Big-Data-Flink/ngsi-ld.html) としても利用可能です。

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/0a602cbb6bbf9351efc2)


## コンテンツ

<details>

<summary><strong>詳細</strong></summary>

-   [リアルタイム処理とビッグデータ分析](#real-time-processing-and-big-data-analysis)
-   [アーキテクチャ](#architecture)
    -   [Flink Cluster の設定](#flink-cluster-configuration)
-   [前提条件](#prerequisites)
    -   [Docker および Docker Compose](#docker-and-docker-compose)
    -   [Maven](#maven)
    -   [Cygwin for Windows](#cygwin-for-windows)
-   [起動](#start-up)
-   [リアルタイム・プロセシング・オペレーション](#real-time-processing-operations)
    -   [Flink 用の JAR ファイルのコンパイル](#compiling-a-jar-file-for-flink)
    -   [コンテキスト・データのストリームの生成](#generating-a-stream-of-context-data)
    -   [ロガー - コンテキスト・データのストリームの読み取り](#logger---reading-context-data-streams)
        -   [ロガー - JAR のインストール](#logger---installing-the-jar)
        -   [ロガー - コンテキスト変更のサブスクライブ](#logger---subscribing-to-context-changes)
        -   [ロガー - 出力の確認](#logger---checking-the-output)
        -   [ロガー - コードの分析](#logger---analyzing-the-code)
    -   [フィードバック・ループ - コンテキスト・データの永続化](#feedback-loop---persisting-context-data)
        -   [フィードバック・ループ - JAR のインストール](#feedback-loop---installing-the-jar)
        -   [フィードバック・ループ - コンテキスト変更のサブスクライブ](#feedback-loop---subscribing-to-context-changes)
        -   [フィードバック・ループ - 出力の確認](#feedback-loop---checking-the-output)
        -   [フィードバック・ループ - コードの分析](#feedback-loop---analyzing-the-code)
-   [次のステップ](#next-steps)

</details>

<a name="real-time-processing-and-big-data-analysis"></a>

# リアルタイム処理とビッグデータ分析

> "Who controls the past controls the future: who controls the present controls the past."
>
> — George Orwell. "1984"

FIWARE に基づくスマート・ソリューションは、マイクロサービスを中心に設計されています。したがって、シンプルな
アプリケーション (スーパーマーケット・チュートリアルなど) から、IoT センサやその他のコンテキスト・データ・プロバイダの
大規模な配列に基づく都市全体のインストールにスケールアップするように設計されています。

関与する膨大な量のデータは、1台のマシンで分析、処理、保存するには膨大な量になるため、追加の分散サービスに作業を委任する
必要があります。これらの分散システムは、いわゆる **ビッグデータ分析** の基礎を形成します。タスクの分散により、開発者は、
従来の方法では処理するには複雑すぎる巨大なデータ・セットから洞察を抽出することができます。隠れたパターンと相関関係を
明らかにします。

これまで見てきたように、コンテキスト・データはすべてのスマート・ソリューションの中核であり、Context Broker は状態の変化を
監視し、コンテキストの変化に応じて、[サブスクリプション・イベント](https://github.com/Fiware/tutorials.Subscriptions)
を発生させることができます。小規模なインストールの場合、各サブスクリプション・イベントは単一の受信エンドポイントで1つずつ
処理できますが、システムが大きくなると、リスナーを圧倒し、潜在的にリソースをブロックし、更新が失われないようにするために
別の手法が必要になります。

**Apache Flink** は、データ・フロー・プロセスの委任を可能にする Java/Scala ベースのストリーム処理フレームワークです。
したがって、イベントの到着時にデータを処理するために、追加の計算リソースを呼び出すことができます。 **Cosmos Flink**
コネクタを使用すると、開発者はカスタム・ビジネスロジックを記述して、コンテキスト・データのサブスクリプション・イベントを
リッスンし、コンテキスト・データのフローを処理できます。 Flink はこれらのアクションを他のワーカーに委任することができ、
そこで必要に応じて順次または並行してアクションが実行されます。データフローの処理自体は、任意に複雑にすることができます。

実際には、明らかに、既存のスーパーマーケット・シナリオは小さすぎてビッグデータ・ソリューションを使用する必要はありませんが、
コンテキスト・データ・イベントの連続ストリームを処理する大規模ソリューションで必要となる可能性のある、リアルタイム処理の
タイプを実証するための基盤として機能します。

<a name="architecture"></a>

# アーキテクチャ

このアプリケーションは、[以前のチュートリアル](https://github.com/FIWARE/tutorials.IoT-Agent/)で作成されたコンポーネントと
ダミー IoT デバイス上に構築されます。 3つの FIWARE コンポーネントを使用します。
[Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/),
[IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/) および Orion を
[Apache Flink cluster](https://ci.apache.org/projects/flink/flink-docs-stable/concepts/runtime.html) クラスタに接続する
ための [Cosmos Orion Flink Connector](https://fiware-cosmos-flink.readthedocs.io/en/latest/) です。Flink クラスタ自体は、
実行を調整する単一の **JobManager** _master_ と、タスクを実行する単一の **TaskManager** _worker_ で構成されます。

Orion Context Broker と IoT Agent はどちらも、オープンソースの [MongoDB](https://www.mongodb.com/) テクノロジーに依存して、
保持している情報の永続性を維持しています。また、[以前のチュートリアル](https://github.com/FIWARE/tutorials.IoT-Agent/)で
作成したダミー IoT デバイスを使用します。

したがって、全体的なアーキテクチャは次の要素で構成されます :

-   独立したマイクロサービスとしての2つの **FIWARE Generic Enablers** :
    -   FIWARE [Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/)は、
        [NGSI-LD](https://forge.etsi.org/swagger/ui/?url=https://forge.etsi.org/gitlab/NGSI-LD/NGSI-LD/raw/master/spec/updated/full_api.json)
        を使用してリクエストを受信します
    -   FIWARE [IoT Agent for UltraLight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/) は、
        [NGSI-LD](https://forge.etsi.org/swagger/ui/?url=https://forge.etsi.org/gitlab/NGSI-LD/NGSI-LD/raw/master/spec/updated/full_api.json)
        を使用してサウスバウンド・リクエストを受信し、それらをデバイス用の
        [UltraLight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual)
        コマンドに変換します

-   [Apache Flink cluster](https://ci.apache.org/projects/flink/flink-docs-stable/concepts/runtime.html) は、
    単一の **JobManager** と単一の **TaskManager ** で構成されます
    -   FIWARE [Cosmos Orion Flink Connector](https://fiware-cosmos-flink.readthedocs.io/en/latest/) は、
        コンテキストの変更をサブスクライブし、リアルタイムでそれらの操作を実際に行うデータフローの一部として
        デプロイされます
-   1つの [MongoDB](https://www.mongodb.com/) **データベース** :
    -   **Orion Context Broker** がデータ・エンティティ、サブスクリプション、レジストレーションなどの
        コンテキスト・データ情報を保持するために使用します
    -   **IoT Agent** がデバイスの URL やキーなどのデバイス情報を保持するために使用します
-   **チュートリアル・アプリケーション** は次のことを行います:
    -   システム内のコンテキスト・エンティティを定義する静的な @context ファイルを提供します
    -   [UltraLight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual)
        を使用してダミーの[農業用 IoT デバイス](https://github.com/FIWARE/tutorials.IoT-Sensors/tree/NGSI-LD)
        のセットとして機能します

全体のアーキテクチャを以下に示します :

![](https://fiware.github.io/tutorials.Big-Data-Flink/img/architecture.png)

要素間の相互作用はすべて HTTP リクエストによって開始されるため、エンティティはコンテナ化され、公開されたポートから
実行できます。

Apache Flink クラスタの設定情報は、関連する `docker-compose.yml` ファイルの `jobmanager` および `taskmanager`
セクションで確認できます :

<a name="flink-cluster-configuration"></a>

## Flink Cluster の設定

```yaml
jobmanager:
    image: flink:1.9.0-scala_2.11
    hostname: jobmanager
    container_name: flink-jobmanager
    expose:
        - "8081"
        - "9001"
    ports:
        - "6123:6123"
        - "8081:8081"
    command: jobmanager
    environment:
        - JOB_MANAGER_RPC_ADDRESS=jobmanager
```

```yaml
taskmanager:
    image: flink:1.9.0-scala_2.11
    hostname: taskmanager
    container_name: flink-taskmanager
    ports:
        - "6121:6121"
        - "6122:6122"
        - "9001:9001"
    depends_on:
        - jobmanager
    command: taskmanager
    links:
        - "jobmanager:jobmanager"
    environment:
        - JOB_MANAGER_RPC_ADDRESS=jobmanager
```

`jobmanager` コンテナは3つのポートでリッスンしています :

-   ポート `8081` が公開されているため、Apache Flink ダッシュボードの Web フロントエンドを確認できます
-   ポート `9001` が公開されていまため、インストレーションがコンテキスト・データのサブスクリプションを
    受信できます
-   ポート `6123` は標準の **JobManager** RPC ポートで、内部通信に使用されます

`taskmanager` コンテナは2つのポートでリッスンしています :

-   ポート `6121` と `6122` が使用され、RPC ポートは **TaskManager** によって、内部通信に使用されます

Flinki クラスタ内のコンテナは、次のように単一の環境変数によって駆動されます。

| キー                    | 値           | 説明                                                      |
| ----------------------- | ------------ | --------------------------------------------------------- |
| JOB_MANAGER_RPC_ADDRESS | `jobmanager` | タスク処理をコーディネートする _master_ Job Manager のURL |


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
[YAML files](https://github.com/FIWARE/tutorials.Big-Data-Flink/tree/NGSI-LD/docker-compose) は、アプリケーション
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
git clone https://github.com/FIWARE/tutorials.Big-Data-Flink.git
cd tutorials.Big-Data-Flink
git checkout NGSI-LD

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

<a name="real-time-processing-operations"></a>

# リアルタイム・プロセシング・オペレーション

**Apache Flink** 内のデータフローは、
[Flink ドキュメント](https://ci.apache.org/projects/flink/flink-docs-release-1.9/concepts/programming-model.html)
で次のように定義されています。

> "Flink プログラムの基本的な構成要素はストリームと変換です。概念的には、ストリームはデータ・レコードの潜在的に終わりの
> ないフローであり、変換は入力として1つ以上のストリームを受け取り、結果として1つ以上の出力ストリームを生成する操作です。
>
> Flink プログラムは、実行されると、ストリームと変換オペレータで構成されるストリーミング・データフローにマッピング
> されます。各データフローは、1つ以上のソースで始まり、1つ以上のシンクで終わります。 データフローは、任意の有向非巡回
> グラフ (DAG) に似ています。反復の構造を介して特殊な形式のサイクルが許可されますが、ほとんどの場合、これを単純化するため
> にこれを変更できます。"

![](https://fiware.github.io/tutorials.Big-Data-Flink/img/streaming-dataflow.png)

つまり、ストリーミング・データフローを作成するには、次のものを指定する必要があります :

-   **Source Operator** としてコンテキスト・データを読み取るためのメカニズム
-   変換操作を定義するビジネスロジック
-   **Sink Operator**としてコンテキスト・データを Context Broker にプッシュバックするメカニズム

`orion-flink.connect.jar` は **Source** と **Sink** の両方の操作を提供します。 したがって、ストリーミング・データフローの
パイプライン操作を接続するために必要な Scala コードを記述するだけです。処理コードは、flink クラスターにアップロードできる
JAR ファイルにコンパイルできます。 以下に2つの例を詳しく説明します。このチュートリアルのすべてのソースコードは、
[cosmos-examples](https://github.com/FIWARE/tutorials.Big-Data-Flink/tree/master/cosmos-examples) ディレクトリ内に
あります。

その他の Flink 処理の例は、
[Apache Flink サイト](https://ci.apache.org/projects/flink/flink-docs-release-1.9/getting-started) および
[Flink Connector の例](https://fiware-cosmos-flink-examples.readthedocs.io/)にあります。

<a name="compiling-a-jar-file-for-flink"></a>

### Flink 用の JAR ファイルのコンパイル

サンプル JAR ファイルをビルドするために必要な前提条件を保持する既存の `pom.xml` ファイルが作成されました。

Orion Flink Connector を使用するには、最初に Maven を使用してアーティファクト (artifact) としてコネクタ JAR を手動で
インストールする必要があります :

```console
cd cosmos-examples
curl -LO https://github.com/ging/fiware-cosmos-orion-flink-connector/releases/download/1.2.4/orion.flink.connector-1.2.4.jar
mvn install:install-file \
  -Dfile=./orion.flink.connector-1.2.4.jar \
  -DgroupId=org.fiware.cosmos \
  -DartifactId=orion.flink.connector \
  -Dversion=1.2.4 \
  -Dpackaging=jar
```

その後、同じディレクトリ内で `mvn package` コマンドを実行することでソースコードをコンパイルできます :

```console
mvn package
```

`cosmos-examples-1.2.jar` という新しい JAR ファイルが `cosmos-examples/target` ディレクトリ内に作成されます。

<a name="generating-a-stream-of-context-data"></a>

### コンテキスト・データのストリームの生成

このチュートリアルでは、コンテキストが定期的に更新されているシステムを監視する必要があります。 ダミー IoT センサを
使用してこれを行うことができます。`http://localhost:3000/device/monitor` でデバイス・モニタ・ページを開き、
**Tractor** の移動を開始します。これは、ドロップ・ダウン・リストから適切なコマンドを選択して `send` ボタンを押すことで
実行できます。デバイスからの測定値の流れは、同じページに表示されます:

![](https://fiware.github.io/tutorials.Big-Data-Flink/img/farm-devices.png)

<a name="logger---reading-context-data-streams"></a>

## ロガー - コンテキスト・データのストリームの読み取り

最初の例では、Orion-LD Context Broker から通知を受信するために、`NGSILDSource` オペレータを使用します。具体的には、
この例では、各タイプのデバイスが1分で送信する通知の数をカウントします。 サンプルのソースコードは
[org/fiware/cosmos/tutorial/LoggerLD.scala](https://github.com/FIWARE/tutorials.Big-Data-Flink/blob/NGSI-LD/cosmos-examples/src/main/scala/org/fiware/cosmos/tutorial/LoggerLD.scala)
にあります。

<a name="logger---installing-the-jar"></a>

### ロガー - JAR のインストール

`http://localhost:8081/#/submit` を開きます

![](https://fiware.github.io/tutorials.Big-Data-Flink/img/submit-logger.png)

新しいジョブを設定します

-   **Filename:** `cosmos-examples-1.2.jar`
-   **Entry Class:** `org.fiware.cosmos.tutorial.LoggerLD`

別の方法は、次のようにコマンドラインで curl を使用することです:

```console
curl -X POST -H "Expect:" -F "jarfile=@/cosmos-examples-1.2.jar" http://localhost:8081/jars/upload
```

<a name="logger---subscribing-to-context-changes"></a>

### ロガー - コンテキスト変更のサブスクライブ

動的コンテキスト・システムが起動して実行されると (`Logger` を実行)、**Flink** にコンテキストの変更を通知する
必要があります。

これは、Orion Context Broker の `/ngsi-ld/v1/subscriptions` エンドポイントに POST リクエストを行うことで実行できます。

-   `NGSILD-Tenant` ヘッダは、これらの設定を使用してプロビジョニングされているため、接続された IoT センサからの
    測定値のみをリッスンするようにサブスクリプションをフィルター処理するために使用されます

-   通知 `uri` は、Flink プログラムがリッスンしている URL と一致する必要があります

-   `throttling` 値は、変更がサンプリングされるレートを定義します

#### :one: リクエスト :

```console
curl -L -X POST 'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'Content-Type: application/ld+json' \
-H 'NGSILD-Tenant: openiot' \
--data-raw '{
  "description": "Notify Flink of all animal and farm vehicle movements",
  "type": "Subscription",
  "entities": [{"type": "Tractor"}, {"type": "Device"}],
  "watchedAttributes": ["location"],
  "notification": {
    "attributes": ["location"],
    "format": "normalized",
    "endpoint": {
      "uri": "http://taskmanager:9001",
      "accept": "application/json"
    }
  },
   "@context": "http://context-provider:3000/data-models/ngsi-context.jsonld"
}'
```

レスポンスは **201 - Created** になります

サブスクリプションが作成されている場合、`/v2/subscriptions` エンドポイントに対して GET リクエストを行うことで、
サブスクリプションが起動しているかどうかを確認できます。

#### :two: リクエスト :

```console
curl -X GET \
'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'NGSILD-Tenant: openiot'
```

#### レスポンス :

```json
[
  {
    "id": "urn:ngsi-ld:Subscription:60216f404dae3a1f22b705e6",
    "type": "Subscription",
    "description": "Notify Flink of all animal and farm vehicle movements",
    "entities": [{"type": "Tractor"}, {"type": "Device"}],
    "watchedAttributes": ["location"],
    "notification": {
      "attributes": ["location"],
      "format": "normalized",
      "endpoint": {
        "uri": "http://taskmanager:9001",
        "accept": "application/json"
      },
      "timesSent": 74,
      "lastNotification": "2021-02-08T17:06:06.043Z"
    },
    "@context": "http://context-provider:3000/data-models/ngsi-context.jsonld"
  }
]
```

レスポンスの `notification` セクション内で、サブスクリプションの正常性を説明するいくつかの追加の  `attributes`
を確認できます

サブスクリプションの基準が満たされている場合、`timesSent` は `0` より大きくなければなりません。ゼロの値は、
サブスクリプションの `subject` が正しくないか、サブスクリプションが間違った `NGSILD-Tenant`
 ヘッダで作成されたことを示します。

`lastNotification` は最新のタイムスタンプである必要があります。そうでない場合、デバイスは定期的にデータを
送信していません。**Tractor** を動かしてスマート・ファームをアクティブ化することを忘れないでください。

`lastSuccess` は `lastNotification` の日付と一致する必要があります-そうでない場合、**Cosmos** はサブスクリプション
を適切に受信していません。ホスト名とポートが正しいことを確認してください。

最後に、サブスクリプションの `status` が `active` であることを確認します。有効期限が切れたサブスクリプションは
実行されません。

<a name="logger---checking-the-output"></a>

### ロガー - 出力の確認

サブスクリプションを1分間実行したままにして、次を実行します :

```console
docker logs flink-taskmanager -f --until=60s > stdout.log 2>stderr.log
cat stderr.log
```

サブスクリプションを作成すると、コンソールの出力は次のようになります :

```
Sensor(Tractor,19)
Sensor(Device,49)
```

<a name="logger---analyzing-the-code"></a>

### ロガー - コードの分析

```scala
package org.fiware.cosmos.tutorial


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector.{NGSILDSource}

object LoggerLD {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = env.addSource(new NGSILDSource(9001))

    // Process event stream
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .map(entity => new Sensor(entity.`type`, 1))
      .keyBy("device")
      .timeWindow(Time.seconds(60))
      .sum(1)

    // print the results with a single thread, rather than in parallel
    processedDataStream.printToErr().setParallelism(1)
    env.execute("Socket Window NgsiLDEvent")
  }
}
case class Sensor(device: String, sum: Int)
```

プログラムの最初の行は、コネクタを含む必要な依存関係をインポートすることを目的としています。次のステップは、コネクタが
提供するクラスを使用して `NGDILDSource` のインスタンスを作成し、Flink が提供する環境に追加することです。

`NGSILDSource` コンストラクタはパラメータとしてポート番号 (`9001`) を受け入れます。このポートは、Orion からの
サブスクリプション通知をリッスンするために使用され、`NgsiEventLD` オブジェクトの `DataStream` に変換されます。これらの
オブジェクトの定義は、
[Orion-Flink Connector ドキュメント](https://github.com/ging/fiware-cosmos-orion-flink-connector/blob/master/README.md#NGSILDSource)
に記載されています。

ストリーム処理は、5つの個別のステップで構成されています。最初のステップ (`flatMap()`) は、一定期間内に受信したすべての
NGSI-LD イベントのエンティティ・オブジェクトをまとめるために実行されます。その後、コードはそれらを `map()` 操作で繰り返し、
目的の属性を抽出します。この場合、センサの `type` (`Device`  or `Tractor`) に関心があります。

各反復内で、必要なプロパティを持つカスタム・オブジェクトを作成します : センサの `type` と各通知の増分。このために、
次のようにケース・クラスを定義できます :

```scala
case class Sensor(device: String, sum: Int)
```

その後、作成されたオブジェクトをデバイスのタイプ (`keyBy("device")`) でグループ化し、それらに対して `timeWindow()` や
`sum()` などの操作を実行できます。

処理後、結果がコンソールに出力されます :

```scala
processedDataStream.print().setParallelism(1)
```

<a name="feedback-loop---persisting-context-data"></a>

## フィードバック・ループ - コンテキスト・データの永続化

2番目の例では、土壌湿度が低すぎる場合に水栓をオンにし、土壌湿度が通常のレベルに戻ったときに水栓をオフに戻します。
このようにして、土壌の湿度は常に適切なレベルに保たれます。

データフロー・ストリームは、通知を受信するために `NGSILDSource` オペレータを使用し、モーション・センサにのみ応答する
ように入力をフィルタし、`NGSILDSink` を使用して処理されたコンテキストを Context Broker にプッシュします。サンプルの
ソースコードは
[org/fiware/cosmos/tutorial/Feedback.scala](https://github.com/FIWARE/tutorials.Big-Data-Flink/blob/master/cosmos-examples/src/main/scala/org/fiware/cosmos/tutorial/FeedbackLD.scala)
にあります。

<a name="feedback-loop---installing-the-jar"></a>

### フィードバック・ループ - JAR のインストール

`http://localhost:8081/#/job/running` を開きます

![](https://fiware.github.io/tutorials.Big-Data-Flink/img/running-jobs.png)

実行中のジョブ (存在する場合) を選択し、**Cancel Job**  をクリックします

その後、`http://localhost:8081/#/submit` を開きます

![](https://fiware.github.io/tutorials.Big-Data-Flink/img/submit-feedback.png)

新しいジョブを設定します

-   **Filename:** `cosmos-examples-1.2.jar`
-   **Entry Class:** `org.fiware.cosmos.tutorial.FeedbackLD`

<a name="feedback-loop---subscribing-to-context-changes"></a>

### フィードバック・ループ - コンテキスト変更のサブスクライブ

この例を実行するには、新しいサブスクリプションを設定する必要があります。 サブスクリプションは、土壌湿度センサの
コンテキストの変化をリッスンしています。

#### :three: リクエスト:

```console
curl -L -X POST 'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'Content-Type: application/ld+json' \
-H 'NGSILD-Tenant: openiot' \
--data-raw '{
  "description": "Notify Flink of changes of Soil Humidity",
  "type": "Subscription",
  "entities": [{"type": "SoilSensor"}],
  "watchedAttributes": ["humidity"],
  "notification": {
    "attributes": ["humidity"],
    "format": "normalized",
    "endpoint": {
      "uri": "http://flink-taskmanager:9001",
      "accept": "application/json"
    }
  },
   "@context": "http://context-provider:3000/data-models/ngsi-context.jsonld"
}'
```

サブスクリプションが作成されている場合は、`/ngsi-ld/v1/subscriptions/` エンドポイントに GET
リクエストを送信することで、サブスクリプションが起動しているかどうかを確認できます。

#### :four: リクエスト:

```console
curl -X GET \
'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'NGSILD-Tenant: openiot'
```

<a name="feedback-loop---checking-the-output"></a>

### フィードバック・ループ - 出力の確認

`http://localhost:3000/device/monitor` を開きます

Farm001の温度を上げ、湿度値が35を下回るまで待ちます。そうすると、水栓が自動的にオンになり、
土壌の湿度が上がります。湿度が50を超えると、水栓も自動的にオフになります。

<a name="feedback-loop---analyzing-the-code"></a>

### フィードバック・ループ - コードの分析

```scala
package org.fiware.cosmos.tutorial


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.fiware.cosmos.orion.flink.connector._

object FeedbackLD {
  final val CONTENT_TYPE = ContentType.JSON
  final val METHOD = HTTPMethod.PATCH
  final val CONTENT = "{\n  \"type\" : \"Property\",\n  \"value\" : \" \" \n}"
  final val HEADERS = Map(
    "NGSILD-Tenant" -> "openiot",
    "Link" -> "<http://context-provider:3000/data-models/ngsi-context.jsonld>; rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json\""
  )
  final val LOW_THRESHOLD = 35
  final val HIGH_THRESHOLD = 50

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = env.addSource(new NGSILDSource(9001))
    // Process event stream
    val processedDataStream = eventStream.flatMap(event => event.entities)
      .filter(ent => ent.`type` == "SoilSensor")

    /* High humidity */

    val highHumidity = processedDataStream
      .filter(ent =>  (ent.attrs("humidity") != null) && (ent.attrs("humidity")("value").asInstanceOf[BigInt] > HIGH_THRESHOLD))
      .map(ent => (ent.id,ent.attrs("humidity")("value")))

    val highSinkStream= highHumidity.map(sensor => {
      OrionSinkObject(CONTENT,"http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:Device:water"+sensor._1.takeRight(3)+"/attrs/off",CONTENT_TYPE,METHOD,HEADERS)
    })

    highHumidity.map(sensor => "Sensor" + sensor._1 + " has detected a humidity level above " + HIGH_THRESHOLD + ". Turning off water faucet!").print()
    OrionSink.addSink( highSinkStream )


    /* Low humidity */
    val lowHumidity = processedDataStream
      .filter(ent => (ent.attrs("humidity") != null) && (ent.attrs("humidity")("value").asInstanceOf[BigInt] < LOW_THRESHOLD))
      .map(ent => (ent.id,ent.attrs("humidity")("value")))

    val lowSinkStream= lowHumidity.map(sensor => {
      OrionSinkObject(CONTENT,"http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:Device:water"+sensor._1.takeRight(3)+"/attrs/on",CONTENT_TYPE,METHOD,HEADERS)
    })

    lowHumidity.map(sensor => "Sensor" + sensor._1 + " has detected a humidity level below " + LOW_THRESHOLD + ". Turning on water faucet!").print()
    OrionSink.addSink( lowSinkStream )

    env.execute("Socket Window NgsiEvent")
  }

  case class Sensor(id: String)
}
```

ご覧のとおり、以前の例に似ています。主な違いは、処理されたデータを **`OrionSink`** を介して Context Broker
に書き戻すことです。

**`OrionSinkObject`** の引数は次のとおりです :

-   **Message**: `"{\n  \"type\" : \"Property\",\n  \"value\" : \" \" \n}"`.
-   **URL**: `"http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:Device:water"+sensor._1.takeRight(3)+"/attrs/on"` or `"http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:Device:water"+sensor._1.takeRight(3)+"/attrs/off"`, depending on whether we are turning on or off the water faucet. TakeRight(3) gets the number of
    the sensor, for example '001'.
-   **Content Type**: `ContentType.Plain`.
-   **HTTP Method**: `HTTPMethod.PATCH`.
-   **Headers**:  `Map("NGSILD-Tenant" -> "openiot", "Link" -> "<http://context-provider:3000/data-models/ngsi-context.jsonld>; rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json\"" )`.
    オプション・パラメータ HTTP リクエストに必要なヘッダを追加します。

<a name="next-steps"></a>

# 次のステップ

高度な機能を追加することで、アプリケーションに複雑さを加える方法を知りたいですか？ このシリーズの
[他のチュートリアル](https://www.letsfiware.jp/fiware-tutorials)を読むことで見つけることができます

---

<a name="licensse"></a>

## License

[MIT](LICENSE) © 2021 FIWARE Foundation e.V.
