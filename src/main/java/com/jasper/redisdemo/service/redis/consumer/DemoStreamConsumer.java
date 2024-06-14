package com.jasper.redisdemo.service.redis.consumer;

import com.jasper.redisdemo.annotation.RegisterConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RegisterConsumer
public class DemoStreamConsumer extends AbstractRedisMessageStreamConsumer {

  public DemoStreamConsumer(RedisTemplate<String, String> redisTemplate) {
    super(redisTemplate, "demoGroup", "demoConsumer-1", "demoStream");
  }

  @Override
  protected void processMessage(MapRecord<String, String, String> message) {
    log.info("[{}][{}][{}] Received message: {}", groupName, consumerName, topic, message);
    acknowledge(message);
  }
}
