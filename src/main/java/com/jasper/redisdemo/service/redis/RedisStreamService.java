package com.jasper.redisdemo.service.redis;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoConsumers;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroups;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoStream;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisStreamService {
  private final RedisTemplate<String, String> redisTemplate;

  public RedisStreamService(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }


  /**
   * Get the pending messages for the stream
   * @param stream
   * @param group
   * @return
   */
  public PendingMessagesSummary getPendingMessages(String stream, String group) {
    return redisTemplate.opsForStream().pending(stream, group);
  }

  public List<MapRecord<String, Object, Object>> getMessageHistory(String stream, String start, String end, Integer count) {
    Range<String> range = getRange(start, end);
    if (count != null) {
      Limit limit = Limit.limit().count(count);
      return redisTemplate.opsForStream().range(stream, range, limit);
    }
    return redisTemplate.opsForStream().range(stream, range);
  }

  /**
   * Get message history in descending order
   * @param stream
   * @param start
   * @param end
   * @return
   */
  public List<MapRecord<String, Object, Object>> getMessageHistoryDesc(String stream, String start, String end, Integer count) {
    Range<String> range = getRange(start, end);
    if (count != null) {
      Limit limit = Limit.limit().count(count);
      return redisTemplate.opsForStream().reverseRange(stream, range, limit);
    }
    return redisTemplate.opsForStream().reverseRange(stream, range);
  }

  /**
   * Get stream info
   * @param stream
   * @return
   */
  public XInfoStream getStreamInfo(String stream) {
    return redisTemplate.opsForStream().info(stream);
  }

  public XInfoGroups getGroupInfo(String stream) {
    return redisTemplate.opsForStream().groups(stream);
  }

  public XInfoConsumers getConsumerInfo(String stream, String group) {
    return redisTemplate.opsForStream().consumers(stream, group);
  }

  /**
   * Trim stream for memory management
   * @param stream
   * @param count
   * @return
   */
  public Long trimStream(String stream, long count) {
    Long trimCount = redisTemplate.opsForStream().trim(stream, count);
    log.info("Trimmed {} messages from stream {}", trimCount, stream);
    return trimCount;
  }

  /**
   * Get stream length
   * @param stream
   * @return
   */
  public Long getStreamLength(String stream) {
    return redisTemplate.opsForStream().size(stream);
  }

  private Range<String> getRange(String start, String end) {
    Range<String> range;
    if (start == null && end == null) {
      range = Range.unbounded();
    }
    else if (start == null) {
      range = Range.leftUnbounded(Bound.inclusive(end));
    }
    else if (end == null) {
      range = Range.rightUnbounded(Bound.inclusive(start));
    }
    else {
      range = Range.closed(start, end);
    }
    return range;
  }
}
