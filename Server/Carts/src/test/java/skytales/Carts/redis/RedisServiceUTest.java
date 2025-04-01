package skytales.Carts.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import org.springframework.data.redis.core.script.RedisScript;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.util.redis.RedisService;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisServiceUTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @InjectMocks
    private RedisService redisService;

    private static final long TTL_SECONDS = 70;
    private static final String CARTS_ZSET_KEY = "cart_terms";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void testGet() {
        String key = "testKey";
        Set<BookItemReference> expectedSet = new HashSet<>(Arrays.asList(new BookItemReference(), new BookItemReference()));
        when(valueOperations.get(key)).thenReturn(expectedSet);

        Set<BookItemReference> result = redisService.get(key);

        assertEquals(expectedSet, result);
    }

    @Test
    void testGetWithLinkedHashMap() {
        String key = "testKey";
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
        when(valueOperations.get(key)).thenReturn(linkedHashMap);

        Set<BookItemReference> result = redisService.get(key);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSet() {
        String key = "testKey";
        Set<BookItemReference> value = new HashSet<>(Arrays.asList(new BookItemReference(), new BookItemReference()));

        redisService.set(key, value);

        verify(valueOperations).set(key, value);
        verify(redisTemplate).expire(key, TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void testGetTerm() {
        String versionKey = "versionKey";
        when(zSetOperations.score(CARTS_ZSET_KEY, versionKey)).thenReturn(5.0);

        int result = redisService.getTerm(versionKey);

        assertEquals(5, result);
    }

    @Test
    void testIncrBy() {
        String versionKey = "versionKey";
        when(zSetOperations.score(CARTS_ZSET_KEY, versionKey)).thenReturn(null);

        redisService.incrBy(versionKey);

        verify(zSetOperations).add(CARTS_ZSET_KEY, versionKey, 0);
        verify(zSetOperations).incrementScore(CARTS_ZSET_KEY, versionKey, 1);
    }

    @Test
    void testReset() {
        String versionKey = "versionKey";

        redisService.reset(versionKey);

        verify(zSetOperations).add(CARTS_ZSET_KEY, versionKey, 0);
    }

    @Test
    void testDelete() {
        String cartKey = "cartKey";

        redisService.delete(cartKey);

        verify(redisTemplate).delete(cartKey);
    }


    @Test
    void testCheckAndCleanMemory() {

        RedisService spyService = spy(redisService);

        doReturn(true).when(spyService).isMemoryFull();

        assertThrows(RedisConnectionFailureException.class, spyService::checkAndCleanMemory);

        verify(spyService, never()).checkAndEvictInactiveCarts(anyInt());
        verify(spyService, never()).checkAndEvictLeastUsedCarts(anyInt());
    }

    @Test
    void testCheckAndEvictInactiveCartsThrowsException() {
        int limit = 10;
        RedisService spyService = spy(redisService);

        doThrow(new RedisConnectionFailureException("Connection failure")).when(redisTemplate).execute(any(RedisScript.class), anyList(), anyInt());

        boolean result = spyService.checkAndEvictInactiveCarts(limit);

        assertFalse(result);
    }







}