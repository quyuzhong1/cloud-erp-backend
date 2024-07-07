package com.common.business.annotation;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;


/**
 * 分布式锁注解
 * <pre>
 * 使用示例：
 * @取id作为分布式锁的key <br/>
 * @DistributeLocker(keyName=”id“)
 * public void updateById(String id){...}
 * @取user实体id和name作为分布式锁的key <br/>
 * @DistributeLocker(keyName="id,name")
 * public void updateUser(SysUserEntity user){...}
 * @取channel对象id作为分布式锁的key   <br/>
 * @DistributeLocker(argIndex=1,keyName="id")
 * public void updateChannel(String sellerId, BasicChannelEntity channel){...}
 * @取channel对象id(集合)作为分布式锁的key集合(批量加锁),支持内嵌集合a->list->list2-..  <br/>
 * @RedissonLocker(keyName="id")
 * public void updateChannel(List<BasicChannelEntity> channelList){...}
 * </pre>
 *
 * @Date 2024/07/05
 */

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributeLocker {
    /**
     * 取第几个参数
     * @return
     */
    int argIndex() default 0;

    /**
     * 指定组成分布式锁的key，以逗号分隔。
     * 如：key="name,age",则分布式锁的key为这两个字段value的拼接 name+"|"+age
     */
    String keyName() default "#";

    /**
     * 业务类型
     * @return
     */
    String businessType() default "";

    /**
     * 最大等待时间(等其他锁释放)
     */
    long waiteTime() default 30;

    /**
     * 锁的有效时间，即：拿到锁后持有锁的时间
     */
    long leaseTime() default 5;

    /**
     * 时间单位,默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
