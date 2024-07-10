package com.common.business.annotation;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;


/**
 * 分布式锁注解
 * @since 2024/07/04
 * @author Edison.Qu
 * Edison.Qu 2024/07/04
 * <p>使用示例：</p>
 * <ul>
 * <font color="red">@DistributeLocker()</font>
 * <li>取id作为分布式锁的key</li>
 * <li>public void updateById(String id){...}</li>
 * </ul>
 * <ul>
 * <font color="red">@DistributeLocker(keyName="id,name")</font>
 * <li>取user实体id和name作为分布式锁的key</li>
 * <li>public void updateUser(SysUserEntity user){...}</li>
 * </ul>
 * <ul>
 * <font color="red">@DistributeLocker(argIndex=1,keyName="id")</font>
 * <li>取channel对象id作为分布式锁的key  </li>
 * <li>public void updateChannel(String sellerId, BasicChannelEntity channel){...}</li>
 * </ul>
 * <ul>
 * <font color="red">@DistributeLocker(keyName="id")</font>
 * <li>取channel对象id(集合)作为分布式锁的key集合(批量加锁),支持内嵌集合a->list->list2-..</li>
 * <li>public void updateChannel(List<BasicChannelEntity> channelList){...}</li>
 * </ul>
 */

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributeLocker {
    /**
     * 取第几个参数
     * @return  参数索引
     */
    int argIndex() default 0;

    /**
     * 指定组成分布式锁的key，以逗号分隔。
     * 如：key="name,age",则分布式锁的key为这两个字段value的拼接 name+"|"+age
     */
    String keyName() default "#";

    /**
     * 业务类型
     * @return  业务类型
     */
    String businessType() default "";

    /**
     * 最大等待时间(等其他锁释放)
     */
    long waiteTime() default 30;

    /**
     * 时间单位,默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
