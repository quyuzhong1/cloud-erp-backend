package com.common.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class RedisLock {
    @Autowired
    private StringRedisTemplate redisTemplate;
    private final Long DEFAULT_TRY_TIME = 10000L;// 默认失败重试时间，10秒
    private final Long DEFAULT_SLEEP_TIME = 100L;// 默认sleep时间，100毫秒
    /**
     * redis锁超时时间
     */
    public static final int LOCK_TIMEOUT = 10 * 1000;

    /**
     * 加锁，如果失败，自动重试
     */
    public boolean aotuTryLock(String key, String value) {
        return tryLock(key, value, true, true, DEFAULT_TRY_TIME);
    }

    /**
     * 加锁，如果失败，重试，直到成功或超出指定时间
     */
    public boolean tryLock(String key, String value, boolean reTry, boolean needTimeOut, long timeOutMillis) {
        //加锁
        boolean iflag = lock(key, value);

        //加锁成功，直接返回
        if (iflag) {
            return iflag;
        }

        //加锁失败，不需要重试，直接返回
        if (!reTry) {
            return iflag;
        }

        //加锁失败, 且已超时，返回
        if (needTimeOut && timeOutMillis <= 0) {
            return false;
        }

        //获取sleep时间
        long sleepMillis = getSleepMillis(needTimeOut, timeOutMillis);

        //sleep后重新获取锁
        sleep(sleepMillis);

        return tryLock(key, value, reTry, needTimeOut, timeOutMillis);
    }

    /**
     * 加锁
     *
     * @param key                                    锁的key
     * @param value：缓存的value（多服务器可以使用redis时间+客户端标识等） 这里存储的是锁的过期时间 = 当前时间 + 超时时间
     * @return 结果
     */
    public boolean lock(String key, String value) {
        //往redis中设置一个key，该key即代表锁。如果可以设置，说明加锁成功。如果不能设置说明加锁失败（可能是被别的线程获得了锁，且未处理完，未释放锁。也可能是死锁等原因导致）
        if (redisTemplate.opsForValue().setIfAbsent(key, value)) {
            return true;
        }

        /*
         * 程序往下走，说明此时加锁失败。为防止死锁现象，需要判断锁是否过期。
         * 如果锁过期，此时会有多个线程进入if里面的加锁方法，对锁设置新的过期时间。
         * 假设当前锁过期时间为d1，当前有两个线程A和B，同时判断到当前锁已过期，并准备获得锁。
         * 假设线程A先获得锁成功，则此时线程A通过getAndSet()方法获得了上一个锁的过期时间d1，且将锁的过期时间设置为了d2。
         * 则此时B通过getAndSet()方法去加锁时，获得上一个锁的时间就会是d2，因为 d2 != d1，线程B就会加锁失败。
         * 这样就保证了同一时间锁只会被一个线程获得。
         */

        //获取锁的过期时间
        String currentValue = redisTemplate.opsForValue().get(key);
        //如果锁过期（获取的当前锁的过期时间不为空，且小于当前时间）
        if (!StringUtils.isEmpty(currentValue)
                && Long.parseLong(currentValue) < System.currentTimeMillis()) {

            //当前线程，获取上一个锁的过期时间，并设置新的过期时间
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);

            //如果上一个锁的过期时间不为空，且当前线程获得上一个锁的过期时间有效，则加锁成功。否则加锁失败。
            return !StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue);
        }

        return false;
    }

    /**
     * 解锁
     *
     * @param key   锁的key
     * @param value 锁的过期时间
     */
    public void unlock(String key, String value) {
        try {
            //获取当前锁的值
            String currentValue = redisTemplate.opsForValue().get(key);

            /*
             * 如果当前锁的值，等于当前线程传进来的锁的值，说明当前锁被当前线程持有，则可以由当前线程解锁。
             * 否则，可能一些网络错误或者IO错误等，造成当前线程获得锁之后，无法正常释放，且超时了。
             * 则此时锁已被别的线程获得，且设置了新的值，此时就不能由当前线程去解锁。
             */
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
                //从redis中删除key，即是删除锁
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            log.error("【redis分布式锁】解锁异常：", e);
        }
    }

    /**
     * 获取休眠时间
     *
     * @param needTimeOut   是否需要判断超时时间
     * @param timeOutMillis 尝试超时时间(毫秒)
     * @return 休眠时间
     */
    private long getSleepMillis(boolean needTimeOut, long timeOutMillis) {
        long sleepMillis = DEFAULT_SLEEP_TIME;
        if (needTimeOut) {
            timeOutMillis = timeOutMillis - DEFAULT_SLEEP_TIME;
            if (timeOutMillis < DEFAULT_SLEEP_TIME) {
                sleepMillis = timeOutMillis;
            }
        }
        return sleepMillis;
    }

    /**
     * 休眠
     *
     * @param sleepMillis 休眠时间
     */
    private void sleep(long sleepMillis) {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
