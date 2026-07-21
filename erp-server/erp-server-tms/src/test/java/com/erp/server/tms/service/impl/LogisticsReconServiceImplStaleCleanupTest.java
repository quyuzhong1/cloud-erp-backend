package com.erp.server.tms.service.impl;

import com.erp.server.tms.service.LogisticsReconDetailSubService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogisticsReconServiceImplStaleCleanupTest {

    private LogisticsReconServiceImpl service;
    private LogisticsReconDetailSubService detailSubService;
    private RLock lock;

    @Before
    public void setUp() {
        service = new LogisticsReconServiceImpl();
        detailSubService = Mockito.mock(LogisticsReconDetailSubService.class);
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        lock = Mockito.mock(RLock.class);
        ReflectionTestUtils.setField(service, "logisticsReconDetailSubService", detailSubService);
        ReflectionTestUtils.setField(service, "redissonClient", redissonClient);
        when(redissonClient.getLock(anyString())).thenReturn(lock);
    }

    @Test
    public void shouldNotCleanupStaleMatchingSubsWhenMainLockIsBusy() {
        when(lock.tryLock()).thenReturn(false);

        Boolean cleaned = ReflectionTestUtils.invokeMethod(
                service, "cleanupStaleMatchingSubsIfMainIdle", "main-1", "test");

        assertFalse(Boolean.TRUE.equals(cleaned));
        verify(detailSubService, never()).failStaleMatchingSubsByMainId(anyString());
        verify(lock, never()).unlock();
    }

    @Test
    public void shouldCleanupStaleMatchingSubsAndUnlockWhenMainIsIdle() {
        when(lock.tryLock()).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        Boolean cleaned = ReflectionTestUtils.invokeMethod(
                service, "cleanupStaleMatchingSubsIfMainIdle", "main-1", "test");

        assertTrue(Boolean.TRUE.equals(cleaned));
        verify(detailSubService).failStaleMatchingSubsByMainId("main-1");
        verify(lock).unlock();
    }

    @Test
    public void matchingStaleThresholdShouldBeConfigurableWithSafeFallback() {
        LogisticsReconDetailSubServiceImpl detailSubServiceImpl = new LogisticsReconDetailSubServiceImpl();

        assertEquals(120L, ((Long) ReflectionTestUtils.invokeMethod(
                detailSubServiceImpl, "resolveMatchingStaleMinutes")).longValue());

        ReflectionTestUtils.setField(detailSubServiceImpl, "matchingStaleMinutes", 30L);
        assertEquals(30L, ((Long) ReflectionTestUtils.invokeMethod(
                detailSubServiceImpl, "resolveMatchingStaleMinutes")).longValue());

        ReflectionTestUtils.setField(detailSubServiceImpl, "matchingStaleMinutes", 0L);
        assertEquals(120L, ((Long) ReflectionTestUtils.invokeMethod(
                detailSubServiceImpl, "resolveMatchingStaleMinutes")).longValue());
    }
}
