package com.cdyt.be.config;

import com.cdyt.be.util.CacheNames;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    private RedisCacheConfiguration baseConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put(CacheNames.CATEGORY_HIERARCHY, baseConfig().entryTtl(Duration.ofMinutes(30)));
        configs.put(CacheNames.ACTIVE_TAGS, baseConfig().entryTtl(Duration.ofMinutes(15)));
        configs.put(CacheNames.TOP_TAGS, baseConfig().entryTtl(Duration.ofMinutes(10)));
        configs.put(CacheNames.ARTICLE_DETAIL_ID, baseConfig().entryTtl(Duration.ofMinutes(10)));
        configs.put(CacheNames.ARTICLE_DETAIL_SLUG, baseConfig().entryTtl(Duration.ofMinutes(10)));
        configs.put(CacheNames.ARTICLE_STATS, baseConfig().entryTtl(Duration.ofMinutes(5)));
        configs.put(CacheNames.PUBLISHED_ARTICLE_STATS, baseConfig().entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig().entryTtl(Duration.ofMinutes(5)))
                .withInitialCacheConfigurations(configs)
                .transactionAware()
                .build();
    }

    /**
     * Simple key generator if needed elsewhere.
     */
    @Bean
    public KeyGenerator simpleKeyGenerator() {
        return (target, method, params) -> java.util.Arrays.asList(target.getClass().getSimpleName(), method.getName(),
                params);
    }
}