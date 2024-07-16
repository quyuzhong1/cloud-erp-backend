package com.erp.server.wms.rocketmq.consumer;


import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.DistributedLockEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.PackageForecastService;
import com.erp.server.wms.service.SoB2cDeliveryDetailService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.ASYNC_MERGE_PACKAGE_DELIVERY_TOPIC,
        selectorExpression = "async_merge_package_delivery_tag",
        consumerGroup = RocketMqConsumerGroup.ASYNC_MERGE_PACKAGE_DELIVERY_CONSUMER,
        consumeThreadNumber = 10
)
public class MergePackageDeliveryConsumer implements RocketMQListener<String> {
    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private RedissonClient redisson;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private PackageForecastService packageForecastService;

    /**
     * 组包处理标记发货和生成销售出库单(勿动)
     */
    @Override
    public void onMessage(String soId) {
        //查询发货单
        List<SoB2cDeliveryEntity> deliveryEntities = soB2cDeliveryService.listBySourceIds(Arrays.asList(soId));
        //过滤掉取消发货
        SoB2cDeliveryEntity curDeliveryEntity = deliveryEntities.stream()
                .filter(v -> !SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(v.getStatus()))
                .findFirst()
                .orElse(null);

        if (null == curDeliveryEntity) {
            return;
        }
        int retryCount = 1;
        // 重试次数key防止无限重试
        String retryCountKey = StrUtil.format(RedisCacheConstants.MERGE_PACKAGE_RETRY_COUNT_KEY, soId);
        Object retryCountObj = redisTemplate.opsForValue().get(retryCountKey);
        if (null != retryCountObj) {
            retryCount = (Integer) retryCountObj;
            if (retryCount > 200) {
                log.warn("【组包处理消费】销售单【{}】重试次数超过100终止消费", curDeliveryEntity.getSoCode());
                return;
            }
            retryCount = retryCount + 1;
            // 记录重试次数
            redisUtil.set(retryCountKey, retryCount, 86400);
        } else {
            redisUtil.set(retryCountKey, 1, 86400);
        }

        // 判断当前单据平台标记发货是否有正在处理
        String signDeliveryKey = StrUtil.format(RedisCacheConstants.MERGE_PACKAGE_SIGN_DELIVERY_KEY, curDeliveryEntity.getDictPlatform(), curDeliveryEntity.getShopId());
        Boolean setSignResult = redisTemplate.opsForValue().setIfAbsent(signDeliveryKey, soId, 30, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(setSignResult)) {
            log.warn("【组包处理消费】销售单【{}】因标记发货处理中重试", curDeliveryEntity.getSoCode());
            // 延时推送到队列重试
            SendResult sendResult = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.ASYNC_MERGE_PACKAGE_DELIVERY_TOPIC, RocketMqTagEnum.ASYNC_MERGE_PACKAGE_DELIVERY_TAG.getName(),
                    soId, soId, convertSignDelayLevel(retryCount));
            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(sendResult)));
            }
            // 当前停止
            return;
        }
        try {
            // 处理标记发货(独立事务)
            handleDelivery(soId, curDeliveryEntity);
        } finally {
            // 释放锁
            redisTemplate.delete(signDeliveryKey);
        }

        // 判断当前单据库存是否有正在处理
        List<SoB2cDeliveryDetailEntity> deliveryDetailList = soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(curDeliveryEntity.getId()));

        // 按扣库存逻辑分组
        Map<String, List<SoB2cDeliveryDetailEntity>> map = deliveryDetailList.stream()
                .collect(Collectors.groupingBy(e -> StrUtil.format("{}_{}_{}", e.getWarehouseId(),
                        StrUtils.null2EmptyWithTrim(e.getWarehouseLocation()),
                        e.getSkuId()
                )));
        // 所有key
        List<String> soOutStockKeyList = new LinkedList<>();
        for (Map.Entry<String, List<SoB2cDeliveryDetailEntity>> entry : map.entrySet()) {
            SoB2cDeliveryDetailEntity deliveryDetail = entry.getValue().get(0);
            // 按照仓库+仓位+库存状态+SKU 进行判断
            String lockKey = StrUtil.format("{}:{}:{}:{}:{}",
                    DistributedLockEnum.WMS_INVENTORY_SKU.getCode(),
                    deliveryDetail.getWarehouseId(),
                    StrUtils.null2EmptyWithTrim(deliveryDetail.getWarehouseLocation()),
                    InventoryStatusEnum.FROZEN.getCode(),
                    deliveryDetail.getSkuId());
            String soOutStockKey = StrUtil.format(RedisCacheConstants.MERGE_PACKAGE_INVENTORY_KEY, lockKey);
            Boolean setSoOutStockResult = redisTemplate.opsForValue().setIfAbsent(soOutStockKey, soId, 30, TimeUnit.SECONDS);
            RLock lock = redisson.getLock(lockKey);
            if (Boolean.FALSE.equals(setSoOutStockResult) || lock.isLocked()) {
                // 重试前释放当前单据所有之前的key
                if (CollectionUtils.isNotEmpty(soOutStockKeyList)) {
                    redisTemplate.delete(soOutStockKeyList);
                }
                log.warn("【组包处理消费】销售单【{}】因生销售出库单或库存处理中重试", curDeliveryEntity.getSoCode());
                // 延时推送到队列重试
                SendResult sendResult = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.ASYNC_MERGE_PACKAGE_DELIVERY_TOPIC, RocketMqTagEnum.ASYNC_MERGE_PACKAGE_DELIVERY_TAG.getName(),
                        soId, soId, convertSoOutStockDelayLevel(retryCount));
                if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                    throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(sendResult)));
                }
                // 当前停止
                return;
            } else {
                soOutStockKeyList.add(soOutStockKey);
            }
        }

        // 生成销售出库单(独立事务)
        log.debug("【组包预报】销售单【{}】生成销售出库单开始", curDeliveryEntity.getSoCode());
        //出库
        try {
            soB2cDeliveryService.generateB2cSoOutstock(curDeliveryEntity);
        } finally {
            if (CollectionUtils.isNotEmpty(soOutStockKeyList)) {
                // 释放当前单据所有之前的key
                redisTemplate.delete(soOutStockKeyList);
            }
        }
        log.debug("【组包预报虚假标记发货】销售单【{}】生成销售出库单结束", curDeliveryEntity.getSoCode());

        //处理其他(独立事务)
        packageForecastService.handleMergePackageDeliveryOther(soId, curDeliveryEntity);
    }

    /**
     * 标记发货处理
     */
    private void handleDelivery(String soId, SoB2cDeliveryEntity curDeliveryEntity) {
        log.debug("【组包预报虚假标记发货】销售单【{}】标记发货开始", curDeliveryEntity.getSoCode());
        //记录需要手动标发的订单id
        Boolean flag = soB2cFeign.checkPlatformShipOrder(soId);
        if (flag) {
            //调用第三方平台SDK发货
            BatchResultDTO resultDTO = new BatchResultDTO();
            try {
                resultDTO = soB2cDeliveryService.falseDelivery(curDeliveryEntity.getId());
            } catch (Exception e) {
                String type = SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode();
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(type);
                addError.setParamJson(soId);
                addError.setReturnJson(resultDTO.toString());
                addError.setMainId(soId);
                addError.setMessage(e.getMessage());
                soB2cFeign.addSoB2cError(addError);
                log.error("【组包预报虚假标记发货】销售单【{}】标记发货失败 >>>错误信息{}", curDeliveryEntity.getSoCode(), ExceptionUtil.stacktraceToString(e));
            }
        }
        log.debug("【组包预报虚假标记发货】销售单【{}】标记发货结束", curDeliveryEntity.getSoCode());
    }

    /**
     * 标记发货重试队列延时等级
     */
    private int convertSignDelayLevel(int retryCount) {
        if (retryCount <= 1){
            // 首次延时等级1=1秒后重试
            return 1;
        } if (retryCount <= 4){
            // 其他延时等级2=5秒后重试
            return 2;
        }else {
            // 其他延时等级3=10秒后重试
            return 3;
        }
    }

    /**
     * 生成销售出库单重试队列延时等级
     */

    private int convertSoOutStockDelayLevel(int retryCount) {
        if (retryCount <= 2){
            // 首次延时等级2=5秒后重试
            return 2;
        }else {
            // 其他延时等级3=10秒后重试
            return 3;
        }
    }
}
