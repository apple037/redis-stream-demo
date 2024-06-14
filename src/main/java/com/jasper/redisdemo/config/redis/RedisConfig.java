package com.jasper.redisdemo.config.redis;

import com.google.gson.Gson;
import com.jasper.redisdemo.config.redis.serializer.GsonRedisSerializer;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {
  private final Environment env;

  public RedisConfig(Environment env) {
    this.env = env;
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    String host = env.getProperty("spring.data.redis.host");
    int port = Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.data.redis.port")));
    log.debug("Redis host: {}, port: {}", host, port);
    if (host == null) {
      throw new IllegalArgumentException("Redis host is required");
    }
    return new LettuceConnectionFactory(host, port);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    // Gson as value serializer
    template.setValueSerializer(new GsonRedisSerializer<Object>(Object.class, new Gson()));
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new GsonRedisSerializer<>(Object.class, new Gson()));
    return template;
  }
}
