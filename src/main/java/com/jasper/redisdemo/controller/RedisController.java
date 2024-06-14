package com.jasper.redisdemo.controller;

import com.jasper.redisdemo.enums.OrderEnum;
import com.jasper.redisdemo.service.redis.RedisStreamService;
import com.jasper.redisdemo.service.redis.publisher.RedisMessageStreamPublisher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoConsumers;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroups;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/redis")
public class RedisController {
  private final RedisMessageStreamPublisher redisMessageStreamPublisher;
  private final RedisStreamService redisStreamService;

  public RedisController(RedisMessageStreamPublisher redisMessageStreamPublisher,
      RedisStreamService redisStreamService) {
    this.redisMessageStreamPublisher = redisMessageStreamPublisher;
    this.redisStreamService = redisStreamService;
  }


  @PostMapping("/send")
  public Object send(HttpServletRequest request,String streamName,String message) {
    System.out.println("message: " + message);
    Map<String, Object> messageMap = new HashMap<>();
    messageMap.put("message", message);
    RecordId id = redisMessageStreamPublisher.sendMessage(streamName, messageMap);
    log.debug("[RecordId] {}", id);
    return id;
  }

  /**
   * 取得尚未 ack 的訊息
   * @param streamName
   * @return
   */
  @GetMapping("/pendings")
  public Object redisPending(String streamName, String groupName) {
    return redisStreamService.getPendingMessages(streamName, groupName);
  }

  @GetMapping("/info")
  public Object redisInfo(String streamName, String groupName) {
    XInfoStream streamInfo = redisStreamService.getStreamInfo(streamName);
    XInfoGroups groupInfo = redisStreamService.getGroupInfo(streamName);
    XInfoConsumers consumerInfo = redisStreamService.getConsumerInfo(streamName, groupName);
    Map<String, Object> result = new HashMap<>();
    result.put("streamInfo", streamInfo);
    result.put("groupInfo", groupInfo);
    result.put("consumerInfo", consumerInfo);
    return result;
  }

  /**
   * 取得歷史訊息
   * @param streamName
   * @param start
   * @param end
   * @return
   */
  @GetMapping("/history/{streamName}")
  public Object redisHistory(@PathVariable String streamName, String start, String end, OrderEnum order, Integer count) {
    Map<String, Object> resultMap = new HashMap<>();

    List<MapRecord<String, Object, Object>> results = new ArrayList<>();
    switch (order) {
      case ASC -> results = redisStreamService.getMessageHistory(streamName, start, end, count);
      case DESC ->
          results = redisStreamService.getMessageHistoryDesc(streamName, start, end, count);
      default -> log.debug("💩?");
    }
    resultMap.put("count",results.size());
    resultMap.put("data",results);
    return resultMap;
  }


  /**
   * 清理 stream 訊息
   * @param streamName
   * @param count
   * @return
   */
  @GetMapping("/trim/{streamName}")
  public Object redisTrim(@PathVariable String streamName, long count) {
    return redisStreamService.trimStream(streamName, count);
  }
}
