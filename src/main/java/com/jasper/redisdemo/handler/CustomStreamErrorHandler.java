package com.jasper.redisdemo.handler;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.util.ErrorHandler;

@Slf4j
public class CustomStreamErrorHandler implements ErrorHandler {

  @Override
  public void handleError(Throwable t) {
    // Redis system exception handling
    // TODO race competition between server gracefully shutdown and stream polling will cause Connection closed exception
    if (t instanceof RedisSystemException) {
      // if caused by Connection closed, ignore it
      if (t.getCause() instanceof RedisException && t.getCause().getMessage().equals("Connection closed")) {
        log.warn("Redis connection closed!");
        return;
      }
      log.error("Redis system exception", t);
    }
  }
}
