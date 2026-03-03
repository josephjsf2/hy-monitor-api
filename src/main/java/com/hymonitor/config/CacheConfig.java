package com.hymonitor.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Cache configuration using Caffeine
 * Provides in-memory caching for frequently accessed data
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Caffeine cache manager
     * @return configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofSeconds(30)));  // 30 second TTL
        return cacheManager;
    }
}
