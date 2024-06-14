package com.jasper.redisdemo.service.redis.publisher;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisMessageStreamPublisher {

  protected final RedisTemplate<String, String> redisTemplate;

  public RedisMessageStreamPublisher(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public RecordId sendMessage(String stream, Map<String, Object> messageMap) {
    return redisTemplate.opsForStream().add(stream, messageMap);
  }
}

