package com.intuit.userbusinessprofile.service;

import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CacheService {
    private final RedisCacheManager cacheManager;

    public CacheService(RedisCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictSingleCacheValue(String cacheName, String cacheKey) {
        int count = 0;
        while(count <=3) {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if(cache!=null)
                    cache.evict(cacheKey);
                break;
            } catch (Exception e){
                count++;
            }
        }
    }


}
