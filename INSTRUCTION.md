### 事前準備:

MacBook前提。

- Mavenをインストールしてください
  - `brew install maven`
  - Mavenに関してはMave 3.x系であれば動くのではないかと思います。
  
### 作業開始:

MacBook前提。

- このレポジトリをgit cloneしてください
  - `git clone git@github.com:mvrck-inc/training-akka-java-2-actor.git`
  
この課題はgit clone下ソースコードをそのまま使うことで、自分で新たにソースコードを書くことなく実行できるようになっています。
もちろん、自分で書き方を覚えたい方や、最後の発展的内容に取り組みたい方はご自分でぜひソースコードを書いてみてください。

---
- アプリケーションを走らせてください
  - `mvn compile`
  - `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.Main`

このコマンドでHTTP APIとアクターのバックエンドが一体になったプロセスが立ち上がります。

```
[INFO] Scanning for projects...
[INFO]
[INFO] ----------------< org.mvrck.training:akka-java-2-actor >----------------
[INFO] Building app 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ akka-java-2-actor ---
SLF4J: A number (1) of logging calls during the initialization phase have been intercepted and are
SLF4J: now being replayed. These are subject to the filtering rules of the underlying logging system.
SLF4J: See also http://www.slf4j.org/codes.html#replay
Server online at http://localhost:8080/
Press RETURN to stop...
```

---
- curlでデータをPOSTしてください
  - `curl -X POST -H "Content-Type: application/json" -d "{\"ticket_id\": 1, \"user_id\": 2, \"quantity\": 1}"  http://localhost:8080/orders`
  - クライアント側ログからレスポンスを確認してください

クライアント側ログで見えるレスポンスはこちら。

```
{"quantity":1,"success":true,"ticketId":1,"userId":2}
```

サーバー側ログには何も表示されません
  
---
- wrkでベンチマークを走らせてください
  - `wrk -t2 -c4 -d5s -s wrk-scripts/order.lua http://localhost:8080/orders`
    - `-t2`: 2 threads
    - `-c4`: 4 http connections
    - `-d5`: 5 seconds of test duration
    - `wrk-scripts/order.lua` ([リンク](./wrk-scrips/order.lua))
    - クライアント側とサーバー側の実行結果を確認してください

<p align="center">
  <img width=640 src="https://user-images.githubusercontent.com/7414320/80275884-56e7f700-871f-11ea-9aa5-caa84abe7ec7.png">
</p>

私のローカル環境で試したところ、結果はこのようになりました。データベースとの接続がまったくないアプリケーションなので、もちろん第一回のトレーニングと比較はできません。

```
Running 5s test @ http://localhost:8080/orders
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    14.38ms   59.98ms 407.71ms   94.39%
    Req/Sec     6.31k     2.85k    9.12k    80.43%
  57856 requests in 5.01s, 10.15MB read
  Non-2xx or 3xx responses: 57756
Requests/sec:  11559.48
Transfer/sec:      2.03MB
```

---
- TicketStockActorとOrderActorの整合性を保つシーケンス図を[確認してください](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuU9IyWW92L1m3F1KKj2rKmZ9JCvEBGakoK_ETamkoI-oKYWeoazEBIvMo2zAIItYGfS7wV47oK1L9nUb9fQaGXGZ6ssZYwAiABMu83-lE9MBoo4rBmNe3W00) - ([参考リンク: PlantUML](https://plantuml.com/sequence-diagram))

まずはシーケンス図によってHTTPから一連のアクターの動作順を確認します。アクターの中身を実装する前に全体の流れを把握しておくとよいでしょう。

![image](https://user-images.githubusercontent.com/7414320/79700726-d7979500-82d2-11ea-8fbb-556ecc79132c.png)

PlantUMLの表現はこちら
```
@startuml
"HTTP API" -> TicketStockActor: process order
TicketStockActor -> OrderActor: create order
"HTTP API" <- OrderActor: response
@enduml
```

---
- TicketStockActorの状態遷移図を[確認してください](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuUAArefLqDMrKqWiIypCIKpAIRLII2vAJIn9rT3aGX8hB4tCAyaigLImKp10YAFhLCXCKyXBBSUdEh-qn3yjk2G_ETiAGxKjK3MIFAe4bqDgNWhGoG00) - ([参考リンク: PlantUML](https://plantuml.com/state-diagram))

次にアクターの状態遷移を定義していきます。アクターの実装を始める前に状態遷移図を描くと考えが整理しやすくなります。

![image](https://user-images.githubusercontent.com/7414320/79700734-e8480b00-82d2-11ea-9848-e78aa97d0bf9.png)

PlantUMLの表現はこちら
```
@startuml
[*] --> available: create()
available: quantity > 0
available --> available:  if new quantity > 0
available --> outOfStock: if new quantity = 0
outOfStock: quantity = 0
@enduml
```

---
- OrderActorの状態遷移図を[確認してください](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuOhMYbNGrRLJI4hDI2pBp2-oKaWkIaqiITNGv798pKi1AW40) - ([参考リンク: PlantUML](https://plantuml.com/state-diagram))

![image](https://user-images.githubusercontent.com/7414320/79700758-0f9ed800-82d3-11ea-9012-32884c5d86f7.png)

PlantUMLの表現はこちら
```
[*] --> behavior: create()
```

---
- 詳細な状態遷移図を見てメッセージ、遷移可能状態、副作用を[確認してください](../)

詳細な状態遷移図では、先程の状態遷移図とは違い、アクターが受け取るメッセージの種類ごとに状態遷移と副作用をまとめます。つまり:
  - (先程の)状態遷移図: 一つのアクターが取りうる全ての状態をまとめる
  - 詳細な状態遷移図: ある一つのメッセージを、ある一つの状態で受け取った時の動作をまとめる

という違いがあります。これによって、よりアクターを実装する際のソースコードに近いヒントを図から得ることが出来ます。
このトレーニングではアクターが単純すぎるのであまり利点を感じられないかもしれませんが、第3回のトレーニングになると大まかな状態遷移図と詳細な状態遷移図を分けるメリットがわかるでしょう。

([リンク](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuOhMYbNGrRLJI2nBpCn9JCf9jL88ACfFJYqkzYzAIItYWekZgrB8J5F8IorNA2nDp2l9BAbKi5CmG5ETNrhYdnPSaf-SZKMvBL2vGsfU2j0H0000))
![image](https://user-images.githubusercontent.com/7414320/79701286-cd779580-82d6-11ea-9d92-776a09e41612.png)

```
@startuml
[*] --> available: ProcessOrder
available --> available:  if new quantity > 0
available --> outOfStock:  if new quantity = 0
@enduml
```

([リンク](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuOhMYbNGrRLJo2yjyKyBBibFphPI22ZAJqujBlOlIaajua98WDK1rLiff1OLvHSf5AKM5-Jd5QToEQJcfG3D0W00))
![image](https://user-images.githubusercontent.com/7414320/79701299-e41dec80-82d6-11ea-8ef8-e9b867db9699.png)

```
@startuml
[*] --> outOfStock: ProcessOrder
outOfStock --> [*]: error response
@enduml
```

([リンク](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuOhMYbNGrRLJI4hDI2pBp2-oKd1FBV4lIaajue89WUM1wgmKWbAB2_BpYbEv75BpKe2w0G00))
![image](https://user-images.githubusercontent.com/7414320/79701311-fe57ca80-82d6-11ea-8f76-d240ed354b73.png)

```
@startuml
[*] --> behavior: GetOrder
behavior --> [*]: Response
@enduml
```

---
- ソースコードのコマンドを[確認してください]
  - TicketStockActor([リンク](./src/main/java/org/mvrck/training/actor/TicketStockActor.java#L47L64))
  - OrderActor([リンク](./src/main/java/org/mvrck/training/actor/TicketStockActor.java#L24L39))

---
- 状態遷移「表」を確認してください

状態遷移表をうまく使えば、場合分けの抜け漏れを減らせます。

| TicketStockActor | ProcessOrder |
|------------------|--------------|
| available        | オーダー処理 |
| outOfStock       | - |

| OrderActor | GetOrder   |
|------------|------------|
| behavior   | Response   |

---
- ソースコードの状態の定義を[確認してください]
  - TicketStockActor([リンク](./src/main/java/org/mvrck/training/actor/TicketStockActor.java#L47L64))
  - OrderActor([リンク](./src/main/java/org/mvrck/training/actor/TicketStockActor.java#L24L39))

---
- ガーディアンアクター以下親子関係のから樹形図を[確認してください](../)

![image](https://user-images.githubusercontent.com/7414320/79701646-ae2e3780-82d9-11ea-8c0d-510cc3d1190c.png)

([リンク](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuUBAJyfAJIvHS2zDB4h9JCo3yKCoaxDJIu9ByfEp0nABKlDAO1B-HIcfHL0XJBM6MCICKBGQel1GvOnHU2PSN31NAUJhwc8w2KKQnM4OIj4DC2Iin8WBoKI43OROXN6eDiOkRCBba9gN0Wn_0000))
```
@startuml
object Guardian
object TicketStockParent
object OrderParent
object TicketStock1
object TicketStock2
object Order1
object Order2
object Order3
object Order4

Guardian o-- TicketStockParent
Guardian o-- OrderParent
TicketStockParent o-- TicketStock1
TicketStockParent o-- TicketStock2
OrderParent o-- Order1
OrderParent o-- Order2
OrderParent o-- Order3
OrderParent o-- Order4
@enduml
```

---
- 各アクターの実装を確認してください
  - GuardianActor([リンク](./src/main/java/org/mvrck/training/http/GuardianActor.java))
    - 他のアクターと違い、GuradianActorのみhttpパッケージ以下に配置しています、なぜならGuardianは内部にakka-httpのstart用のコードを持つので、httpパッケージに依存しているからです
    - もしGuardianをactorパッケージにおいてしまうと、actor.Guardianはhttpパッケージに依存し、httpパッケージはactorに依存するという循環が起きてしまいます
  - TicketStockParentActor([リンク](./src/main/java/org/mvrck/training/actor/TicketStockParentActor.java))
  - TicketStockActor([リンク](./src/main/java/org/mvrck/training/actor/TicketStockActor.java))
  - OrderParentActor([リンク](./src/main/java/org/mvrck/training/actor/OrderParentActor.java))
  - OrderActor([リンク](./src/main/java/org/mvrck/training/actor/OrderActor.java))

### 発展的内容:

- 状態遷移図で売り切れ後のチケット追加販売を考えてください
- 状態遷移図でオーダーのキャンセルを考慮してください
- 状態遷移図でイベントの中止、払い戻しを考えてください
- 状態遷移図で先着と抽選の2通りを考えてください
- 状態遷移図で複数チケットの同時購入を考えてください
- 不正データのハンドリング、業務例外を考えてください
  - 不正なオーダーを弾いてください(年齢制限、不正なチケット種別の組み合わせ、などなど) 
  - 購入履歴と照らし合わせた不正な購入を防いでください
- asyncテストが必要となるテストケース例を考えてください
- コンサート以外に、スポーツや映画、入場券のみイベントを実現するテーブルを考えてください
