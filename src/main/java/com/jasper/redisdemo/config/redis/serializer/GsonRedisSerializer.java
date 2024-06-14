package com.jasper.redisdemo.config.redis.serializer;
import com.google.gson.Gson;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class GsonRedisSerializer<T> implements RedisSerializer<T> {

  private final Class<T> clazz;
  private final Gson gson;

  public GsonRedisSerializer(Class<T> clazz, Gson gson) {
    this.clazz = clazz;
    this.gson = gson;
  }

  @Override
  public byte[] serialize(T t) throws SerializationException {
    if (t == null) {
      return new byte[0];
    }
    return gson.toJson(t).getBytes();
  }

  @Override
  public T deserialize(byte[] bytes) throws SerializationException {
    if (bytes == null || bytes.length == 0) {
      return null;
    }
    return gson.fromJson(new String(bytes), clazz);
  }
}
