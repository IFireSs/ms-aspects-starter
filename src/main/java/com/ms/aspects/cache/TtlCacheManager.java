package com.ms.aspects.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TtlCacheManager {

    private final Map<String, Map<Object, Entry>> caches = new ConcurrentHashMap<>();

    public Optional<Object> get(String cacheName, Object key) {
        Map<Object, Entry> cache = caches.get(cacheName);
        if (cache == null) return Optional.empty();
        Entry e = cache.get(key);
        if (e == null) return Optional.empty();
        if (e.expiresAt > 0 && System.currentTimeMillis() > e.expiresAt) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.ofNullable(e.value);
    }

    public void put(String cacheName, Object key, Object value, long ttlMs) {
        caches.computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>())
              .put(key, new Entry(value, ttlMs > 0 ? System.currentTimeMillis() + ttlMs : 0));
    }

    public void invalidate(String cacheName, Object key) {
        Map<Object, Entry> cache = caches.get(cacheName);
        if (cache != null) cache.remove(key);
    }

    public void invalidateAll(String cacheName) {
        Map<Object, Entry> cache = caches.get(cacheName);
        if (cache != null) cache.clear();
    }

    @Getter
    @ToString
    @AllArgsConstructor
    private static class Entry {
        Object value;
        long expiresAt;
    }
}
