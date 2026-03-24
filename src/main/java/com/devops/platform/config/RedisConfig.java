package com.devops.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Configuration
 * 
 * Configures Redis for caching and session management.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    // Cache names
    public static final String CACHE_METRICS = "metrics";
    public static final String CACHE_APPLICATIONS = "applications";
    public static final String CACHE_INCIDENTS = "incidents";
    public static final String CACHE_USERS = "users";
    public static final String CACHE_THRESHOLDS = "thresholds";
    public static final String CACHE_COST_SUMMARY = "cost_summary";

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Metrics cache - short TTL (1 minute) for real-time data
        cacheConfigurations.put(CACHE_METRICS, defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        // Applications cache - longer TTL (30 minutes)
        cacheConfigurations.put(CACHE_APPLICATIONS, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Incidents cache - medium TTL (5 minutes)
        cacheConfigurations.put(CACHE_INCIDENTS, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Users cache - longer TTL (1 hour)
        cacheConfigurations.put(CACHE_USERS, defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Thresholds cache - longer TTL (1 hour)
        cacheConfigurations.put(CACHE_THRESHOLDS, defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Cost summary cache - medium TTL (15 minutes)
        cacheConfigurations.put(CACHE_COST_SUMMARY, defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
