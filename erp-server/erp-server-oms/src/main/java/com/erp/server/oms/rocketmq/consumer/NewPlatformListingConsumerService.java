package com.erp.server.oms.rocketmq.consumer;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.PlatformProductDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 下载平台商品消费服务
 *
 * @author Jim
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_PRODUCT_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_PLATFORM_PRODUCT_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_PRODUCT_TO_OMS_GROUP)
public class NewPlatformListingConsumerService implements RocketMQListener<Object>{
	@Resource
	private PlatformListingConsumerService platformListingConsumerService;

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
        log.info("监听到平台商品需要同步：entity={}", jsonObject);
        PlatformProductDTO entity = JSON.parseObject(ext.toString(),  PlatformProductDTO.class);
        
        String billNo = entity.getUniqueId();
        String redisKey = "dmp:oms:"+ entity.getPlatform() +":product:" + billNo;
        int count = 1;
        while(!redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 30, TimeUnit.SECONDS)) {
        	log.warn("同步平台商品输出任务正在执行中：{}，重试获取锁次数：{}" , billNo , count);
        	count = count + 1;
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
        }
        try {
        	platformListingConsumerService.handle(ext);
        } catch (Throwable e) {
            log.error("平台商品同步失败，msg = {}",e.getMessage());
            updateDTO.setStatus(DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode());
            updateDTO.setResponseData("平台商品消费数据失败：" + ExceptionUtil.stacktraceToOneLineString(e));
            updateDTO.setMessage(e.getMessage());
        }
        dmpInoutTaskFeign.updateOutputTaskRecord(updateDTO);
	}
	
}
