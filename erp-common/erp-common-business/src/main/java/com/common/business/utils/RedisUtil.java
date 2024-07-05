package com.common.business.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 指定缓存失效时间
     *
     * @param key
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * @param key
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除键值
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     * 获取键值
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置键值
     *
     * @param key
     * @param value
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置键值同时指定过期时间
     *
     * @param key
     * @param value
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 键值递增
     *
     * @param key
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 键加1
     * @param key
     * @return
     */
    public long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 键值递减
     *
     * @param key
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    /**
     * 获取HashGet
     *
     * @param key
     * @param item
     * @return
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 设置HashGet
     *
     * @param key
     * @return
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }
    public <T> T getHashMap(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }
    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void putAllHashMap(final String key, final Map<String, T> dataMap) {
        redisTemplate.opsForHash().putAll(key, dataMap);
    }
    /**
     * 设置多个HashGet
     *
     * @param key
     * @param map
     * @return
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 设置多个HashGet时指定过期时间
     *
     * @param key
     * @param map
     * @param time 时间(秒)
     * @return
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key
     * @param item
     * @param value
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("redis hset error", e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key
     * @param item
     * @param value
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key
     * @param item
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否存在item
     *
     * @param key
     * @param item
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key
     * @param item
     * @param by   要增加的值(大于0)
     * @return
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key
     * @param item
     * @param by   要减少的值(小于0)
     * @return
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    /**
     * 获取set的内容
     *
     * @param key
     * @return
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {

            return null;
        }
    }

    /**
     * 判断set中是否存在某个值
     *
     * @param key
     * @param value
     * @return
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 设置set
     *
     * @param key
     * @param values
     * @return
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {

            return 0;
        }
    }

    /**
     * 设置set同时指定过期时间
     *
     * @param key
     * @param time   时间(秒)
     * @param values
     * @return
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0)
                expire(key, time);
            return count;
        } catch (Exception e) {

            return 0;
        }
    }

    /**
     * 获取set的元素个数
     *
     * @param key
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {

            return 0;
        }
    }

    /**
     * 从set中移除元素
     *
     * @param key
     * @param values
     * @return
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {

            return 0;
        }
    }

    /**
     * 获取List的内容
     *
     * @param key
     * @param start 开始索引
     * @param end   结束索引 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {

            return null;
        }
    }

    /**
     * 获取list的长度
     *
     * @param key
     * @return
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {

            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {

            return null;
        }
    }

    /**
     * 设置List
     *
     * @param key
     * @param value
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 设置List
     *
     * @param key
     * @param value
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 设置List
     *
     * @param key
     * @param value
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 设置List同时指定过期时间
     *
     * @param key
     * @param value
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key
     * @param index
     * @param value
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key
     * @param count
     * @param value
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {

            return 0;
        }
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> multiGet(final String key, final Collection hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }


    /**
     * redis分布式锁
     * @Author Luo_WG
     * @Date 2024/7/5 10:50
     * @param lockKey
     * @param lockValue
     * @param lockSeconds 锁的过期时间
     * @param blockAndGet 没有获取到时是否阻塞再获取
     * @return boolean
     **/
    public boolean lockAutoUnlock(String lockKey, String lockValue, long lockSeconds, boolean blockAndGet) throws Exception {
        return lockAutoUnlock(lockKey, lockValue, lockSeconds, blockAndGet, redisTemplate);
    }

    /**
     * redis分布式锁
     * @Author Luo_WG
     * @Date 2024/7/4 16:25
     * @param lockKey
     * @param lockValue
     * @param lockSeconds 锁的过期时间
     * @param blockAndGet 没有获取到时是否阻塞再获取
     * @return boolean
     **/
    private boolean lockAutoUnlock(String lockKey, String lockValue, long lockSeconds, boolean blockAndGet, RedisTemplate<String, String> redisTemplate) throws Exception {
        if (ObjectUtils.isEmpty(lockKey) || ObjectUtils.isEmpty(lockValue)) {
            return false;
        }
        String lockValue2 = redisTemplate.opsForValue().get(lockKey);
        if (!ObjectUtils.isEmpty(lockValue2)) {
            return false;
        }
        SessionCallback<Boolean> callback = new SessionCallback<Boolean>() {
            @Override
            public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException {
                //开启redis事务
                operations.multi();
                redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue);
                //加锁成功,设置过期时间,防止死锁
                redisTemplate.expire(lockKey, lockSeconds, TimeUnit.SECONDS);
                //返回执行结果
                List<Object> result = operations.exec();
                if (result != null && result.size() > 0) {
                    return (Boolean) result.get(0);
                }
                return false;
            }
        };
        Boolean isLock = redisTemplate.execute(callback);
        if (isLock) {
            //加锁成功
            return true;
        } else {
            if (blockAndGet) {
                int failCount = 0;
                boolean flag = false;
                //再次尝试获取锁三次
                while (failCount < 3) {
                    Thread.sleep(lockSeconds / 3 * 1000);
                    flag = redisTemplate.execute(callback);
                    if (flag) {
                        return true;
                    }
                    failCount++;
                }
                return false;
            } else {
                return false;
            }
        }
    }

    /**
     * 释放锁
     * @param lockKey
     * @param lockValue
     */
    public void unLock(String lockKey, String lockValue) {
        unLock(lockKey, lockValue, redisTemplate);
    }

    /**
     * 释放锁
     * @Author Luo_WG
     * @Date 2024/7/4 16:25
     * @param lockKey
     * @param lockValue
     * @param redisTemplate
     * @return void
     **/
    private void unLock(String lockKey, String lockValue, RedisTemplate<String, String> redisTemplate) {
        try {
            //锁剩余存活时间
            long expireTimeOld = redisTemplate.getExpire(lockKey, TimeUnit.MILLISECONDS);
            String value = redisTemplate.opsForValue().get(lockKey);
            if (null != value && value.equals(lockValue)) {
                //delete前锁剩余存活时间
                long expireNew = redisTemplate.getExpire(lockKey, TimeUnit.MILLISECONDS);
                if (expireTimeOld > 0 && expireNew > 0 && expireNew > expireTimeOld) {
                    //说明是新获得锁的线程,不能删除
                    return;
                } else {
                    if (expireNew < 1000) {
                        Thread.sleep(1000);
                    } else {
                        //防止因锁过期,导致当前线程删除另一个线程获得的锁,预留1s删除响应时间
                        redisTemplate.delete(lockKey);
//						writelog(new Date()+":"+Thread.currentThread().getName()+": 删除,释放锁成功...");
                    }
                }
            }
        } catch (Exception e) {
            return;
        }
    }
}
