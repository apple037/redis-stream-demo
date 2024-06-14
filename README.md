# Redis Stream Demo

這是一個簡單的 Redis Stream 示範，展示如何利用 Redis 的 MQ 功能來實現消息隊列。

## Getting Started
Redis 的 MQ 功能透過 Stream 實現，使用 RedisTemplate 可以操作 Stream。Stream key 等同於 Topic，Group 則是 Consumer Group。

### 範例說明
- **Stream**: demoStream
- **Consumer Groups**:
    - group1: 兩個 Consumers - consumer1, consumer2
    - group2: 一個 Consumer - consumer3

當 demoStream 有訊息進來時，group1 的 consumer1 和 consumer2 只會有一個收到訊息，但 group2 的 consumer3 會收到訊息。

## Configuration

### Publisher
發布訊息的實現類別：[RedisMessageStreamPublisher.java](src/main/java/com/jasper/redisdemo/service/redis/publisher/RedisMessageStreamPublisher.java)

### Consumer
實現 Consumer 的類別必須實現 `StreamListener` 接口。可參考 [AbstractRedisMessageStreamConsumer](src/main/java/com/jasper/redisdemo/service/redis/consumer/AbstractRedisMessageStreamConsumer.java)

### Container
Spring Boot 啟動時需將 Consumer 註冊到 Container 並啟動。註冊時可以選擇 `receive` 或 `receiveAutoAck`，後者會自動進行 Acknowledge。

- **實作範例**: [RedisConsumerInit](src/main/java/com/jasper/redisdemo/startup/RedisConsumerInit.java)
- **特別錯誤處理**: Consumer 會啟動 polling task，當 Spring Boot 進行優雅停機時會出現 race competition，拋出 `RedisSystemException: Connection closed`。目前透過 CustomExceptionHandler 處理。
    - **錯誤處理實作**: [CustomStreamErrorHandler](src/main/java/com/jasper/redisdemo/handler/CustomStreamErrorHandler.java)

在 `@PreDestroy` 方法中將 Consumer 的訂閱關閉並將 Container 關閉。

## Usage

1. **發送訊息**: `/redis/api/redis/send`
2. **查看待處理訊息**: `/redis/api/redis/pendings`
3. **查看歷史訊息**: `/redis/api/redis/history/{streamName}`
4. **查看 Redis Info**: `/redis/api/redis/info`
5. **清除歷史訊息**: `/redis/api/redis/trim/{streamName}`

這些路徑提供了基本的 API 用於操作和查看 Redis Stream 的各項功能。

## Run with Docker
1. **Build Image**: `docker build -t redis-stream-demo .`
2. **Run Container**: `docker run -p 8080:8080 redis-stream-demo`
3. 需有可供連線的 Redis 伺服器 於配置檔中設定 `spring.data.redis.host`。

## Run with Docker Compose
1. **Build Image**: `docker-compose build`
2. **Run Container**: `docker-compose up`
3. **Stop Container**: `docker-compose down`
4. 包含 Redis 對外開放 6380 port 欲調整，請修改 `docker-compose.yml` 中的 `ports`
