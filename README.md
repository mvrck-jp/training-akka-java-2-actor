JavaによるAkkaトレーニング第2回 

## アクターによる非同期処理

Akkaは非同期処理を実装するのに有用なツールキットです。
今回はAkkaのアクターを使った非同期処理を紹介し、3層アーキテクチャで用いたデータベース・トランザクションによる排他制御との違いを学びます。

<p align="center">
  <img width=640 src="https://user-images.githubusercontent.com/7414320/79640175-17735500-81cb-11ea-94b3-0141d47be6ae.png">
</p>

- [第1回のトレーニング: リレーショナル・データベースのトランザクションによる排他制御](https://github.com/mvrck-inc/training-akka-java-1-preparation)
- [第2回のトレーニング: アクターによる非同期処理](https://github.com/mvrck-inc/training-akka-java-2-actor)
- [第3回のトレーニング: アクターとデータベースのシステム(イベント・ソーシング)](https://github.com/mvrck-inc/training-akka-java-3-persistence)
- [第4回のトレーニング: アクターとデータベースのシステム(CQRS)](https://github.com/mvrck-inc/training-akka-java-4-cqrs)
- [第5回のトレーニング: クラスタリング](https://github.com/mvrck-inc/training-akka-java-5-clustering)

## 課題

この課題をこなすことがトレーニングのゴールです。
独力でも手を動かしながら進められるようようになっていますが、可能ならトレーナーと対話しながらすすめることでより効果的に学べます。

- [課題提出トレーニングのポリシー](https://github.com/mvrck-inc/training-akka-java-1-preparation/blob/master/POLICIES.md)


## この課題で身につく能力

- akkaのアクターを使って素早くアプリケーションの原型を作成できる
- 状態遷移図をもとにアクターの実装をソースコードに書き起こせる

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

- アプリケーションを走らせてください
  - `mvn compile`
  - `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.Main`
- curlでデータをPOSTしてください
  - `curl -X POST -H "Content-Type: application/json" -d "{\"ticket_id\": 1, \"user_id\": 2, \"quantity\": 1}"  http://localhost:8080/orders`
  - クライアント側ログからレスポンスを確認してください
- wrkでベンチマークを走らせてください
  - `wrk -t2 -c4 -d5s -s wrk-scripts/order.lua http://localhost:8080/orders`
    - `-t2`: 2 threads
    - `-c4`: 4 http connections
    - `-d5`: 5 seconds of test duration
    - `wrk-scripts/order.lua` ([リンク](./wrk-scrips/order.lua))
    - クライアント側とサーバー側の実行結果を確認してください
- TicketStockActorとOrderActorの整合性を保つシーケンス図を[確認してください](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuU9IyWW92L1m3F1KKj2rKmZ9JCvEBGakoK_ETamkoI-oKYWeoazEBIvMo2zAIItYGfS7wV47oK1L9nUb9fQaGXGZ6ssZYwAiABMu83-lE9MBoo4rBmNe3W00) - ([参考リンク: PlantUML](https://plantuml.com/sequence-diagram))
- TicketStockActorの状態遷移図を[確認してください](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuUAArefLqDMrKqWiIypCIKpAIRLII2vAJIn9rT3aGX8hB4tCAyaigLImKp10YAFhLCXCKyXBBSUdEh-qn3yjk2G_ETiAGxKjK3MIFAe4bqDgNWhGoG00) - ([参考リンク: PlantUML](https://plantuml.com/state-diagram))
- OrderActorの状態遷移図を[確認してください](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuOhMYbNGrRLJI4hDI2pBp2-oKaWkIaqiITNGv798pKi1AW40) - ([参考リンク: PlantUML](https://plantuml.com/state-diagram))
- 詳細な状態遷移図を見てメッセージ、遷移可能状態、副作用を確認してください([リンク1](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuOhMYbNGrRLJI2nBpCn9JCf9jL88ACfFJYqkzYzAIItYWekZgrB8J5F8IorNA2nDp2l9BAbKi5CmG5ETNrhYdnPSaf-SZKMvBL2vGsfU2j0H0000)) ([リンク2](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuOhMYbNGrRLJo2yjyKyBBibFphPI22ZAJqujBlOlIaajua98WDK1rLiff1OLvHSf5AKM5-Jd5QToEQJcfG3D0W00))([リンク3](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuOhMYbNGrRLJI4hDI2pBp2-oKd1FBV4lIaajue89WUM1wgmKWbAB2_BpYbEv75BpKe2w0G00))
- ソースコードのコマンドを[確認してください]
  - TicketStockActor([リンク](./src/main/java/org/mvrck/training/actor/TicketStockActor.java#L47L64))
  - OrderActor([リンク](./src/main/java/org/mvrck/training/actor/TicketStockActor.java#L47L64))
- 状態遷移「表」を確認してください
- ソースコードの状態の定義を[確認してください]
  - TicketStockActor([リンク](./src/main/java/org/mvrck/training/actor/TicketStockActor.java#L66L78))
  - OrderActor([リンク](./src/main/java/org/mvrck/training/actor/OrderActor.java#L66L79))
- ガーディアンアクター以下親子関係のから樹形図を確認してください ([リンク](http://www.plantuml.com/plantuml/uml/SoWkIImgAStDuUBAJyfAJIvHS2zDB4h9JCo3yKCoaxDJIu9ByfEp0nABKlDAO1B-HIcfHL0XJBM6MCICKBGQel1GvOnHU2PSN31NAUJhwc8w2KKQnM4OIj4DC2Iin8WBoKI43OROXN6eDiOkRCBba9gN0Wn_0000))
- 各アクターの実装を確認してください
  - GuardianActor([リンク](./src/main/java/org/mvrck/training/http/GuardianActor.java))
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

## 説明

- [課題背景](./BACKGROUND.md)
- [課題手順の詳細](./INSTRUCTION.md)

## 参考文献・資料

- https://plantuml.com/