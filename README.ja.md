[![FIWARE Banner](https://fiware.github.io/tutorials.Big-Data-Analysis/img/fiware.png)](https://www.fiware.org/developers)

[![FIWARE Context processing, analysis and visualisation](https://nexus.lab.fiware.org/static/badges/chapters/processing.svg)](https://github.com/FIWARE/catalogue/blob/master/processing/README.md)
[![License: MIT](https://img.shields.io/github/license/fiware/tutorials.Big-Data-Analysis.svg)](https://opensource.org/licenses/MIT)
[![Support badge](https://nexus.lab.fiware.org/repository/raw/public/badges/stackoverflow/fiware.svg)](https://stackoverflow.com/questions/tagged/fiware)
[![NGSI v2](https://img.shields.io/badge/NGSI-v2-5dc0cf.svg)](https://fiware-ges.github.io/orion/api/v2/stable/) <br/>
[![Documentation](https://img.shields.io/readthedocs/fiware-tutorials.svg)](https://fiware-tutorials.rtfd.io)

このチュートリアルは [FIWARE Cosmos Orion Flink Connector](http://fiware-cosmos-flink.rtfd.io) の紹介です。これは、最も
人気のあるビッグデータ・プラットフォームの1つである [Apache Flink](https://flink.apache.org/) との統合により、コンテキスト
・データのビッグデータ分析を容易にします。Apache Flink は、無制限および有界のデータ・ストリーム上でステートフルな計算を
行うためのフレームワークおよび分散処理エンジンです。Flink は、すべての一般的なクラスタ環境で実行され、メモリ内の速度と
任意の規模で計算を実行するように設計されています。

チュートリアルでは [cUrl](https://ec.haxx.se/) コマンドを使用しますが、
[Postman ドキュメント](https://fiware.github.io/tutorials.Big-Data-Analysis/) としても利用可能です。

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/fb0de86dea21e2073054)


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
        [NGSI-v2](https://fiware.github.io/specifications/OpenAPI/ngsiv2) を使用してリクエストを受信します
    -   FIWARE [IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/) は、ダミー IoT
        デバイスから Ultralight 2.0 形式のノースバウンド測定値を受信し、Context Broker の
        [NGSI-v2](https://fiware.github.io/specifications/OpenAPI/ngsiv2) リクエストに変換して、コンテキスト・
        エンティティの状態を変更します
-   [Apache Flink cluster](https://ci.apache.org/projects/flink/flink-docs-stable/concepts/runtime.html) は、
    単一の **JobManager** と単一の **TaskManager ** で構成されます
    -   FIWARE [Cosmos Orion Flink Connector](https://fiware-cosmos-flink.readthedocs.io/en/latest/) は、
        コンテキストの変更をサブスクライブし、リアルタイムでそれらの操作を実際に行うデータフローの一部として
        デプロイされます
-   1つの [MongoDB](https://www.mongodb.com/) **データベース** :
    -   **Orion Context Broker** がデータ・エンティティ、サブスクリプション、レジストレーションなどの
        コンテキスト・データ情報を保持するために使用します
    -   **IoT Agent** がデバイスの URL やキーなどのデバイス情報を保持するために使用します
-   3つの**コンテキスト・プロバイダ** :
    -   HTTP 上で実行される
        [Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual)
        を使用する、[ダミー IoT デバイス](https://github.com/FIWARE/tutorials.IoT-Sensors) のセットとして
        機能する Webサーバ
    -   **在庫管理フロントエンド** は、このチュートリアルでは使用しません。次のことを行います :
        -   ストア情報を表示し、ユーザがダミー IoT デバイスと対話できるようにします
        -   各ストアで購入できる製品を表示します
        -   ユーザが製品を "購入" して在庫数を減らすことを許可します
    -   **Context Provider NGSI** プロキシは、このチュートリアルでは使用しません。次のことを行います :
        -   [NGSI-v2](https://fiware.github.io/specifications/OpenAPI/ngsiv2) を使用してリクエストを受信します
        -   独自形式の独自 API を使用して、公開されているデータソースへのリクエストを行います
        -   コンテキスト・データを[NGSI-v2](https://fiware.github.io/specifications/OpenAPI/ngsiv2) 形式で
            Orion Context Broker に返します

全体のアーキテクチャを以下に示します :

![](https://fiware.github.io/tutorials.Big-Data-Analysis/img/architecture.png)

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
        - "9001:9001"
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

![](https://fiware.github.io/tutorials.Big-Data-Analysis/img/streaming-dataflow.png)

つまり、ストリーミング・データフローを作成するには、次のものを指定する必要があります :

-   **Source Operator** としてコンテキスト・データを読み取るためのメカニズム
-   変換操作を定義するビジネスロジック
-   **Sink Operator**としてコンテキスト・データを Context Broker にプッシュバックするメカニズム

`orion-flink.connect.jar` は **Source** と **Sink** の両方の操作を提供します。 したがって、ストリーミング・データフローの
パイプライン操作を接続するために必要な Scala コードを記述するだけです。処理コードは、flink クラスターにアップロードできる
JAR ファイルにコンパイルできます。 以下に2つの例を詳しく説明します。このチュートリアルのすべてのソースコードは、
[cosmos-examples](https://github.com/FIWARE/tutorials.Big-Data-Analysis/tree/master/cosmos-examples) ディレクトリ内に
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
mvn install:install-file \
  -Dfile=./orion.flink.connector-1.2.3.jar \
  -DgroupId=org.fiware.cosmos \
  -DartifactId=orion.flink.connector \
  -Dversion=1.2.3 \
  -Dpackaging=jar
```

その後、同じディレクトリ内で `mvn package` コマンドを実行することでソースコードをコンパイルできます :

```console
cd cosmos-examples
mvn package
```

`cosmos-examples-1.0.jar` という新しい JAR ファイルが `cosmos-examples/target` ディレクトリ内に作成されます。

<a name="generating-a-stream-of-context-data"></a>

### コンテキスト・データのストリームの生成

このチュートリアルでは、コンテキストが定期的に更新されるシステムを監視する必要があります。これを行うには、ダミー IoT
センサーを使用できます。`http://localhost:3000/device/monitor` のデバイス・モニターのページを開き、**Smart Door** の
ロックを解除して、**Smart Lamp** をオンにします。 これは、ドロップ・ダウン・リストから適切なコマンドを選択し、`send`
ボタンを押すことで実行できます。デバイスからの測定値のストリームは、同じページで見ることができます :

![](https://fiware.github.io/tutorials.Big-Data-Analysis/img/door-open.gif)

<a name="logger---reading-context-data-streams"></a>

## ロガー - コンテキスト・データのストリームの読み取り

最初の例では、Orion Context Broker から通知を受信するために、`OrionSource` オペレータを使用します。具体的には、
この例では、各タイプのデバイスが1分で送信する通知の数をカウントします。 サンプルのソースコードは
[org/fiware/cosmos/tutorial/Logger.scala](https://github.com/FIWARE/tutorials.Big-Data-Analysis/blob/master/cosmos-examples/src/main/scala/org/fiware/cosmos/tutorial/Logger.scala)
にあります。

<a name="logger---installing-the-jar"></a>

### ロガー - JAR のインストール

`http://localhost:8081/#/submit` を開きます

![](https://fiware.github.io/tutorials.Big-Data-Analysis/img/submit-logger.png)

新しいジョブを設定します

-   **Filename:** `cosmos-examples-1.0.jar`
-   **Entry Class:** `org.fiware.cosmos.tutorial.Logger`

<a name="logger---subscribing-to-context-changes"></a>

### ロガー - コンテキスト変更のサブスクライブ

動的コンテキスト・システムが起動して実行されると (`Logger` を実行)、**Flink** にコンテキストの変更を通知する
必要があります。

これは、Orion Context Broker の `/v2/subscription` エンドポイントに POST リクエストを行うことで実行できます。

-   `fiware-service` および `fiware-servicepath` ヘッダは、これらの設定を使用してプロビジョニングされている
    ため、接続された IoT センサからの測定値のみをリッスンするようにサブスクリプションをフィルター処理する
    ために使用されます

-   通知 URL は、Flink プログラムがリッスンしている URL と一致する必要があります

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
    "url": "http://jobmanager:9001
  }
}'
```

レスポンスは **201 - Created** になります

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
                "url": "http://jobmanager:9001"
            },
            "lastSuccess": "2019-09-09T09:36:33.00Z",
            "lastSuccessCode": 200
        }
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

<a name="logger---checking-the-output"></a>

### ロガー - 出力の確認

サブスクリプションを1分間実行したままにして、次を実行します :

```console
docker logs flink-taskmanager -f --until=60s > stdout.log 2>stderr.log
cat stderr.log
```

サブスクリプションを作成すると、コンソールの出力は次のようになります :

```
Sensor(Bell,3)
Sensor(Door,4)
Sensor(Lamp,7)
Sensor(Motion,6)
```

<a name="logger---analyzing-the-code"></a>

### ロガー - コードの分析

```scala
package org.fiware.cosmos.tutorial


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector.{OrionSource}

object Logger{

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

プログラムの最初の行は、コネクタを含む必要な依存関係をインポートすることを目的としています。次のステップは、コネクタが
提供するクラスを使用して `OrionSource` のインスタンスを作成し、Flink が提供する環境に追加することです。

`OrionSource` コンストラクタはパラメータとしてポート番号 (`9001`) を受け入れます。このポートは、Orion からの
サブスクリプション通知をリッスンするために使用され、`NgsiEvent` オブジェクトの `DataStream` に変換されます。これらの
オブジェクトの定義は、
[Orion-Flink Connector ドキュメント](https://github.com/ging/fiware-cosmos-orion-flink-connector/blob/master/README.md#orionsource)
に記載されています。

ストリーム処理は、5つの個別のステップで構成されています。最初のステップ (`flatMap()`) は、一定期間内に受信したすべての
NGSI イベントのエンティティ・オブジェクトをまとめるために実行されます。その後、コードはそれらを `map()` 操作で繰り返し、
目的の属性を抽出します。この場合、センサの `type` (`Door`, `Motion`, `Bell` or `Lamp`) に関心があります。

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

2番目の例では、モーション・センサが動きを検出するとランプをオンにします。

データフロー・ストリームは、通知を受信するために `OrionSource` オペレータを使用し、モーション・センサにのみ応答する
ように入力をフィルタし、`OrionSink` を使用して処理されたコンテキストを Context Broker にプッシュします。サンプルの
ソースコードは
[org/fiware/cosmos/tutorial/Feedback.scala](https://github.com/FIWARE/tutorials.Big-Data-Analysis/blob/master/cosmos-examples/src/main/scala/org/fiware/cosmos/tutorial/Feedback.scala)
にあります。

<a name="feedback-loop---installing-the-jar"></a>

### フィードバック・ループ - JAR のインストール

`http://localhost:8081/#/job/running` を開きます

![](https://fiware.github.io/tutorials.Big-Data-Analysis/img/running-jobs.png)

実行中のジョブ (存在する場合) を選択し、**Cancel Job**  をクリックします

その後、`http://localhost:8081/#/submit` を開きます

![](https://fiware.github.io/tutorials.Big-Data-Analysis/img/submit-feedback.png)

新しいジョブを設定します

-   **Filename:** `cosmos-examples-1.0.jar`
-   **Entry Class:** `org.fiware.cosmos.tutorial.Feedback`

<a name="feedback-loop---subscribing-to-context-changes"></a>

### フィードバック・ループ - コンテキスト変更のサブスクライブ

以前の例を実行していない場合は、新しいサブスクリプションを設定する必要があります。モーション・センサが動きを検出した
ときにのみ通知をトリガーするように、より限定したサブスクリプションを設定できます。

> **注 :** 以前のサブスクリプションが既に存在する場合、2番目のより限定した Motion のみのサブスクリプションを作成する
> この手順は不要です。Scala タスク自体のビジネスロジック内にフィルタがあります。


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
      "url": "http://taskmanger:9001"
    }
  }
}'
```

<a name="feedback-loop---checking-the-output"></a>

### フィードバック・ループ - 出力の確認

`http://localhost:3000/device/monitor` を開きます

いずれかのストア内で、ドアのロックを解除して待機します。ドアが開いてモーション・センサがトリガーされると、ランプが
直接オンになります。

<a name="feedback-loop---analyzing-the-code"></a>

### フィードバック・ループ - コードの分析

```scala
package org.fiware.cosmos.tutorial


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector._


object Feedback{
  final val CONTENT_TYPE = ContentType.Plain
  final val METHOD = HTTPMethod.POST
  final val CONTENT = "{  \"on\": {      \"type\" : \"command\",      \"value\" : \"\"  }}"
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
  processedDataStream.printToErr().setParallelism(1)

    val sinkStream = processedDataStream.map(node => {
      new OrionSinkObject("urn:ngsi-ld:Lamp"+ node.id.takeRight(3)+ "@on","http://${IP}:3001/iot/lamp"+ node.id.takeRight(3),CONTENT_TYPE,METHOD)
    })
    OrionSink.addSink(sinkStream)
    env.execute("Socket Window NgsiEvent")
  }

  case class Sensor(id: String)
}
```

ご覧のとおり、コードは以前の例に似ています。主な違いは、処理されたデータを **`OrionSink`** を介して Context Broker
に書き戻すことです。

**`OrionSinkObject`** の引数は次のとおりです :

-   **Message**: `"{ \"on\": { \"type\" : \"command\", \"value\" : \"\" }}"`. 'on' コマンドを送信します
-   **URL**: `"http://localhost:1026/v2/entities/Lamp:"+node.id.takeRight(3)+"/attrs"`.  TakeRight(3) は部屋の番号を
    取得します。例 : '001'
-   **Content Type**: `ContentType.Plain`.
-   **HTTP Method**: `HTTPMethod.POST`.
-   **Headers**: `Map("fiware-service" -> "openiot","fiware-servicepath" -> "/","Accept" -> "*/*")`. オプション・パラメータ
    HTTP リクエストに必要なヘッダを追加します。

<a name="next-steps"></a>

# 次のステップ

高度な機能を追加することで、アプリケーションに複雑さを加える方法を知りたいですか？ このシリーズの
[他のチュートリアル](https://www.letsfiware.jp/fiware-tutorials)を読むことで見つけることができます

---

<a name="licensse"></a>

## License

[MIT](LICENSE) © 2020 FIWARE Foundation e.V.
