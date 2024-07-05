package com.erp.server.wms.rocketmq.consumer;



import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * B2C 销售出库 金蝶同步到WMS
 *
 * @author Lambda
 * @Classname KingdeeB2CSoOutstockConsumer
 * @Date 2023-06-27 10:21
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_KINGDEE_SO_OUTSTOCK_TO_WMS_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_KINGDEE_SO_OUTSTOCK_TO_WMS_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_KINGDEE_SO_OUTSTOCK_TO_WMS_GROUP)
public class KingdeeNewB2CSoOutstockConsumer implements RocketMQListener<Object> {

    @Resource
    private SyncB2CSoOutstockService syncB2CSoOutstockService;

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;
    
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void onMessage(Object ext) {
        //json数据
    	JSONObject jsonObject = JSON.parseObject(ext.toString());
        String dmpOutputTaskRecordId = jsonObject.get("dmpOutputTaskRecordId").toString();

        DmpOutputTaskRecordDTO.UpdateDTO updateDTO = new DmpOutputTaskRecordDTO.UpdateDTO();
        updateDTO.setId(dmpOutputTaskRecordId);
        updateDTO.setStatus(DmpOutputTaskRecordStatusEnum.FINISH.getCode());
        log.info("监听到金蝶销售出库单需要同步：entity={}", jsonObject);
        KingdeeDeliveryDetailEntity entity = JSON.parseObject(ext.toString(),  KingdeeDeliveryDetailEntity.class);
        
        String billNo = entity.getFBillNo();
        String redisKey = "dmp:wms:soOutstock:" + billNo;
        int count = 1;
        while(!redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 30, TimeUnit.SECONDS)) {
        	log.warn("同步销售出库单输出任务正在执行中：{}，重试获取锁次数：{}" , billNo , count);
        	count = count + 1;
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
        }
        try {
        	syncB2CSoOutstockService.syncKingdeeSoOutstock(entity);
        } catch (Throwable e) {
            log.error("金蝶直接销售出库单同步失败，msg = {}",e.getMessage());
            updateDTO.setStatus(DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode());
            updateDTO.setResponseData("消费数据失败：" + ExceptionUtil.stacktraceToOneLineString(e));
        }
        dmpInoutTaskFeign.updateOutputTaskRecord(updateDTO);
    }
}
