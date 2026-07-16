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
     * 最大等待时间(等其他锁释放) key,用于动态获取等待时间
     * @return  key
     */
    String waitTimeKey() default "";

    /**
     * 时间单位,默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 是否在事务提交/回滚后再解锁。
     * <p><b>发起方</b>：本服务通过 {@code @GlobalTransactional} 开启全局事务，或仅处于 Spring 本地事务时，
     * 锁延迟到事务提交/回滚后释放（含 XXL-JOB 等无 HTTP 上下文的发起方）。</p>
     * <p><b>参与方</b>：入站 Feign 请求携带 {@code TX_XID} 时，Seata {@code TransactionHook}
     * 不会在本进程触发，切面退化为<b>方法执行结束后</b>即解锁（非全局事务结束），以避免锁泄漏；
     * 方法上的本地 {@code @Transactional} 仍先于 unlock 完成提交。</p>
     * @return  true:在事务结束后解锁（参与方实际为方法结束后解锁）
     */
    boolean unlockAfterTx() default false;
    /**
     * 重试次数
     * @return  重试次数
     */
    int maxRetries() default 2;

    /**
     * 重试间隔时间(毫秒)
     * @return  重试间隔时间
     */
    long retryIntervalMillis() default 2000;
}
