package com.jasper.redisdemo.service.redis.consumer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.stream.StreamListener;

@Slf4j
@Getter
public abstract class AbstractRedisMessageStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

  protected final String groupName;
  protected final String consumerName;
  protected final String topic;
  protected final RedisTemplate<String, String> redisTemplate;

  public AbstractRedisMessageStreamConsumer(RedisTemplate<String, String> redisTemplate, String groupName, String consumerName,
      String topic) {
    this.redisTemplate = redisTemplate;
    this.groupName = groupName;
    this.consumerName = consumerName;
    this.topic = topic;
  }

  public void ensureStreamAndGroupExist() {
    StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();
    // 檢查Stream是否存在，不存在則創建
    if (Boolean.FALSE.equals(redisTemplate.hasKey(topic))) {
      streamOps.createGroup(topic, ReadOffset.latest(), groupName);
    } else {
      try {
        // 檢查Group是否存在，不存在則創建
        streamOps.groups(topic).stream()
            .filter(group -> group.groupName().equals(groupName))
            .findAny()
            .orElseGet(() -> {
              streamOps.createGroup(topic, ReadOffset.latest(), groupName);
              return null;
            });
      } catch (Exception e) {
        // 如果Stream已存在但Group不存在，會引發異常，需要捕獲並創建Group
        streamOps.createGroup(topic, ReadOffset.latest(), groupName);
      }
    }
  }

  @Override
  public void onMessage(MapRecord<String, String, String> message) {
    try {
      processMessage(message);
    } catch (Exception e) {
      handleFailure(message, e);
      throw e;
    }
  }

  protected abstract void processMessage(MapRecord<String, String, String> message);

  protected void handleFailure(MapRecord<String, String, String> message, Exception e) {
    // Override this method to handle failures
    log.error("Failed to process message: {}", message, e);
  }

  // ack
  public Long acknowledge(MapRecord<String, String, String> message) {
    return redisTemplate.opsForStream().acknowledge(groupName, message);
  }

  // delete
  public Long delete(MapRecord<String, String, String> message) {
    return redisTemplate.opsForStream().delete(message);
  }
}

