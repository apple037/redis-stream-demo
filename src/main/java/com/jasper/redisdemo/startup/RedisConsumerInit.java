package com.jasper.redisdemo.startup;

import com.jasper.redisdemo.annotation.RegisterConsumer;
import com.jasper.redisdemo.handler.CustomStreamErrorHandler;
import com.jasper.redisdemo.service.redis.consumer.AbstractRedisMessageStreamConsumer;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class RedisConsumerInit implements CommandLineRunner {

  private final RedisConnectionFactory redisConnectionFactory;
  private final ApplicationContext applicationContext;

  public RedisConsumerInit(RedisConnectionFactory redisConnectionFactory,
      ApplicationContext applicationContext) {
    this.redisConnectionFactory = redisConnectionFactory;
    this.applicationContext = applicationContext;
  }

  private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
  private final List<Subscription> subscriptions = new ArrayList<>();


  @Override
  public void run(String... args) throws Exception {
    // find all redis consumers
    List<AbstractRedisMessageStreamConsumer> consumers = getAbstractRedisMessageStreamConsumers();
    if (consumers.isEmpty()) {
      return;
    }
    // create container options with custom error handler
    StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
        StreamMessageListenerContainerOptions.builder()
            .errorHandler(new CustomStreamErrorHandler())
            .pollTimeout(Duration.ofSeconds(2))
            .build();

    // add consumers to the redis stream message listener container
    container = StreamMessageListenerContainer.create(redisConnectionFactory, options);
    for (AbstractRedisMessageStreamConsumer consumer : consumers) {
      // ensure stream and group exist
      consumer.ensureStreamAndGroupExist();
      // add consumer to the container
      Subscription subscription = container.receive(
          Consumer.from(consumer.getGroupName(), consumer.getConsumerName()),
          StreamOffset.create(consumer.getTopic(), ReadOffset.lastConsumed()),
          consumer
      );
      // add subscription to the list
      subscriptions.add(subscription);
    }
    container.start();

    log.debug("[原神啟動!]");
  }

  @PreDestroy
  public void shutdown() {
    if (subscriptions != null && !subscriptions.isEmpty()) {
      for (Subscription subscription : subscriptions) {
//        log.debug("Cancelling subscription: {}", subscription);
        subscription.cancel();
      }
    }
    if (container != null) {
      container.stop(() -> log.debug("[原神已停止!]"));
    }
  }

  private List<AbstractRedisMessageStreamConsumer> getAbstractRedisMessageStreamConsumers() {
    // find all beans of type AbstractRedisMessageStreamConsumer
    Map<String, AbstractRedisMessageStreamConsumer> consumers =
        applicationContext.getBeansOfType(AbstractRedisMessageStreamConsumer.class);
    log.debug("Found {} consumers", consumers.size());
    // with annotation RegisterConsumer enable = true
    consumers.values().removeIf(consumer -> {
      RegisterConsumer annotation = consumer.getClass().getAnnotation(RegisterConsumer.class);
      return annotation != null && !annotation.enable();
    });
    log.debug("Found {} enabled consumers", consumers.size());
    for (Map.Entry<String, AbstractRedisMessageStreamConsumer> entry : consumers.entrySet()) {
      log.info("Consumer: {}", entry.getValue().getClass().getSimpleName());
    }
    if (consumers.isEmpty()) {
      log.info("No consumers found");
      return new ArrayList<>();
    }
    return new ArrayList<>(consumers.values());
  }
}
