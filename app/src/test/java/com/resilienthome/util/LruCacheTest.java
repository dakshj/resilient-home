package com.resilienthome.util;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LruCacheTest {

    private static final int CACHE_SIZE = 2;

    @Test
    public void shouldHaveLatestTwoEntriesWhenNotQueried() {
        LruCache<String, String> cache = new LruCache<>(CACHE_SIZE);
        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");

        Map<String, String> expectedResultMap = new LinkedHashMap<>();
        expectedResultMap.put("b", "2");
        expectedResultMap.put("c", "3");

        assertEquals(cache.toString(), expectedResultMap.toString());
    }

    @Test
    public void shouldHaveRecentlyUsedTwoEntriesWhenQueried() {
        LruCache<String, String> cache = new LruCache<>(CACHE_SIZE);
        cache.put("a", "1");
        cache.put("b", "2");
        cache.get("a");
        cache.put("c", "3");

        Map<String, String> expectedResultMap = new LinkedHashMap<>();
        expectedResultMap.put("a", "1");
        expectedResultMap.put("c", "3");

        assertEquals(cache.toString(), expectedResultMap.toString());
    }
}
