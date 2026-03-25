package com.vibemyself.common.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @InjectMocks RedisService redisService;

    @Test
    void save_TTL포함저장() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        redisService.save("key", "value", 60L);
        then(valueOperations).should().set("key", "value", 60L, TimeUnit.SECONDS);
    }

    @Test
    void get_값조회() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("key")).willReturn("value");
        assertThat(redisService.get("key")).isEqualTo("value");
    }

    @Test
    void get_없는키_null반환() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("nokey")).willReturn(null);
        assertThat(redisService.get("nokey")).isNull();
    }

    @Test
    void delete_키삭제() {
        redisService.delete("key");
        then(redisTemplate).should().delete("key");
    }
}
