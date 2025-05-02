package hongik.triple.inframodule.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisClient {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Redis에 key-value 저장
     * @param key 저장할 key
     * @param value 저장할 value
     * @param timeout 데이터 유효시간 (expire time)
     */
    public void setValue(String key, String value, Long timeout) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, value, Duration.ofMinutes(timeout));
    }

    /**
     * Redis에서 key로 value 조회
     * @param key 조회할 key
     * @return key에 해당하는 value
     */
    public String getValue(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();

        return values.get(key);
    }

    /**
     * Redis에서 key로 value 삭제
     * @param key 삭제할 key
     */
    public void deleteValue(String key) {
        // Redis 정상 동작 시에만 삭제
        if(isRedisAvailable()) {
            redisTemplate.delete(key);
        }
    }

    /**
     * Redis 서버가 정상적으로 동작하는지 확인
     * @return Redis 서버 동작 여부
     */
    private boolean isRedisAvailable() {
        try {
            return Optional.ofNullable(redisTemplate.getConnectionFactory())
                    .map(connectionFactory -> (connectionFactory.getConnection().ping() != null))
                    .orElse(Boolean.TRUE);
        } catch (Exception e) {
            return false;
        }
    }
}
