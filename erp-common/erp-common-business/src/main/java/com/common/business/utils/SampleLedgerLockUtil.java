package com.common.business.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.DistributedLockEnum;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 样品台账分布式锁工具类
 * 实现一锁二判三放行的通用逻辑
 * 
 * @author system
 * @since 2025-01-20
 */
@Slf4j
@Component
public class SampleLedgerLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 样品台账分布式锁操作
     * 实现一锁二判三放行的通用逻辑
     * 
     * @param sampleLedgerIds 样品台账ID列表
     * @param operation 需要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithLock(List<String> sampleLedgerIds, Supplier<T> operation) {
        if (CollUtil.isEmpty(sampleLedgerIds)) {
            log.warn("样品台账ID列表为空，直接执行操作");
            return operation.get();
        }

        // 去重并排序，避免死锁
        List<String> sortedLedgerIds = sampleLedgerIds.stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        if (CollUtil.isEmpty(sortedLedgerIds)) {
            log.warn("过滤后的样品台账ID列表为空，直接执行操作");
            return operation.get();
        }

        // 构建锁key列表
        List<String> lockKeys = sortedLedgerIds.stream()
                .map(ledgerId -> DistributedLockEnum.SAMPLE_LEDGER.keyBuilder(ledgerId))
                .collect(java.util.stream.Collectors.toList());

        log.info("开始获取样品台账分布式锁，锁数量：{}，锁keys：{}", lockKeys.size(), lockKeys);

        // 获取分布式锁
        List<RLock> locks = lockKeys.stream()
                .map(redissonClient::getLock)
                .collect(java.util.stream.Collectors.toList());

        boolean locked = false;
        try {
            // 尝试获取所有锁，等待时间30秒，持有时间60秒
            locked = tryLockAll(locks, 30, 60, TimeUnit.SECONDS);
            
            if (!locked) {
                throw new ServiceException("获取样品台账分布式锁失败，请稍后重试");
            }

            log.info("成功获取样品台账分布式锁，开始执行操作");
            
            // 执行操作
            return operation.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取样品台账分布式锁被中断", e);
            throw new ServiceException("获取样品台账分布式锁被中断");
        } catch (Exception e) {
            log.error("样品台账分布式锁操作异常", e);
            throw e;
        } finally {
            // 释放锁
            if (locked) {
                unlockAll(locks);
                log.info("已释放样品台账分布式锁");
            }
        }
    }

    /**
     * 样品台账分布式锁操作（单个台账ID）
     * 
     * @param sampleLedgerId 样品台账ID
     * @param operation 需要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String sampleLedgerId, Supplier<T> operation) {
        if (StrUtil.isBlank(sampleLedgerId)) {
            log.warn("样品台账ID为空，直接执行操作");
            return operation.get();
        }
        
        return executeWithLock(java.util.Collections.singletonList(sampleLedgerId), operation);
    }

    /**
     * 尝试获取所有锁
     * 
     * @param locks 锁列表
     * @param waitTime 等待时间
     * @param leaseTime 持有时间
     * @param timeUnit 时间单位
     * @return 是否成功获取所有锁
     * @throws InterruptedException 中断异常
     */
    private boolean tryLockAll(List<RLock> locks, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        if (CollUtil.isEmpty(locks)) {
            return true;
        }

        // 使用Redisson的联锁功能
        if (locks.size() == 1) {
            return locks.get(0).tryLock(waitTime, leaseTime, timeUnit);
        } else {
            // 多个锁使用联锁
            RLock multiLock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));
            return multiLock.tryLock(waitTime, leaseTime, timeUnit);
        }
    }

    /**
     * 释放所有锁
     * 
     * @param locks 锁列表
     */
    private void unlockAll(List<RLock> locks) {
        if (CollUtil.isEmpty(locks)) {
            return;
        }

        for (RLock lock : locks) {
            try {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("释放锁异常", e);
            }
        }
    }
}
