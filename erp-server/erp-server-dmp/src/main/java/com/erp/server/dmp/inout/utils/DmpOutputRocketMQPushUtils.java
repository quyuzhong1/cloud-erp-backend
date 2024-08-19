package com.erp.server.dmp.inout.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpOutputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmpOutputRocketMQPushUtils{
	
	@Autowired
	private DmpHandlerCache dmpHandlerCache;
	@Autowired
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	@Autowired
	private DmpCfgOutputService dmpCfgOutputService;
	@Autowired
	private DmpOutputTaskService dmpOutputTaskService;
	@Resource
    private MQProducerService mqProducerService;
	@Resource
    private RedisTemplate<String,Object> redisTemplate;
	@Autowired
	@Qualifier("dmpOutputExecutorPool")
	private ExecutorService dmpOutputExecutorPool;
	
	private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");
	
	public void dealDmpOutputTaskRecordEntityList(List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList) {
    	if(CollUtil.isEmpty(dmpOutputTaskRecordEntityList)) {
    		return;
    	}
		
    	Map<String, String> cfgOutputIdEntityMaps = dmpOutputTaskService.lambdaQuery()
    			.in(DmpOutputTaskEntity::getId, dmpOutputTaskRecordEntityList.stream().map(DmpOutputTaskRecordEntity::getMainId).collect(Collectors.toSet()))
    			.select(DmpOutputTaskEntity::getId , DmpOutputTaskEntity::getCfgOutputId)
    			.list().stream().collect(Collectors.toMap(DmpOutputTaskEntity::getId, DmpOutputTaskEntity::getCfgOutputId));
    	Map<String, String> outputTypeIdMaps = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getId, cfgOutputIdEntityMaps.values())
    		.select(DmpCfgOutputEntity::getId , DmpCfgOutputEntity::getTypeId).list().stream().collect(Collectors.toMap(DmpCfgOutputEntity::getId, DmpCfgOutputEntity::getTypeId));
    	
    	int i = 0;
		for(DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity : dmpOutputTaskRecordEntityList) {
			dmpOutputExecutorPool.execute(() -> {
				String cfgOutputId = cfgOutputIdEntityMaps.get(dmpOutputTaskRecordEntity.getMainId());
				if(StringUtils.isBlank(cfgOutputId)) {
					return;
				}
				String typeId = outputTypeIdMaps.get(cfgOutputId);
				if(StringUtils.isBlank(typeId)) {
					return;
				}
				String id = dmpOutputTaskRecordEntity.getId();
				String dataId = dmpOutputTaskRecordEntity.getDataId();
				String redisKey = "dmp:output:task:" + dataId;
				if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 3600, TimeUnit.SECONDS)) {
					try {
						String requestData = dmpOutputTaskRecordEntity.getRequestData();
						DmpCfgMqEntity dmpCfgMqEntity = dmpHandlerCache.getRocketMQDmpCfgMqCache(typeId);
						if(dmpCfgMqEntity == null) {
							return;
						}
						RocketMQTemplate rocketMQTemplate = dmpHandlerCache.getRocketMQTemplate(typeId);
						if(rocketMQTemplate == null) {
							return;
						}
						
						String status = DmpOutputTaskRecordStatusEnum.MQSUCCESS.getCode();
						String responseData = "";
						
						Message<String> rocketMQMessage = MessageBuilder.withPayload(requestData)
				                .setHeader("KEYS", id)
				                .build();
						
						SendResult syncSend = rocketMQTemplate.syncSend(StrUtil.format("{}:{}" , dmpCfgMqEntity.getTopic(), dmpCfgMqEntity.getTag()), rocketMQMessage);
						if (!SendStatus.SEND_OK.equals(syncSend.getSendStatus())){
							status = DmpOutputTaskRecordStatusEnum.MQERROR.getCode();
							responseData = StrUtil.format("发送RocketMQ数据异常，id=：{}，mq信息：{}", id , JSON.toJSONString(dmpCfgMqEntity));
						}
						this.updateStatus(id, status, responseData , "发送RocketMQ数据异常");
					} catch (Exception e) {
						this.updateStatus(id, DmpOutputTaskRecordStatusEnum.MQERROR.getCode(), "发送RocketMQ前失败" + ExceptionUtil.stacktraceToOneLineString(e) , "发送RocketMQ前失败");
					}finally {
						redisTemplate.delete(redisKey);
					}
				}else {
					log.error(redisKey + "任务正在执行中");
				}
			});
			
			i = i + 1;
			if(i % 3 == 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
    	}
	}
	
	public boolean updateStatus(String id , String status , String responseData , String message) {
		Integer errorCount = null;
		String code = "";
		if(status.contains(DmpOutputTaskRecordStatusEnum.ERROR.getCode())) {
			DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = dmpOutputTaskRecordService.getById(id);
			errorCount = dmpOutputTaskRecordEntity.getErrorCount();
			if(!responseData.contains("数据已被他人锁住，为避免数据错误，请稍后再试")) {
				errorCount = errorCount + 1;
			}
			if(errorCount >= 3 && errorCount%3 == 0) {
				status = DmpOutputTaskRecordStatusEnum.ERROR.getCode();
				code = dmpOutputTaskRecordEntity.getSourceCode();
			}
		}
		boolean update = dmpOutputTaskRecordService.lambdaUpdate()
			.eq(DmpOutputTaskRecordEntity::getId, id)
			.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
			.set(DmpOutputTaskRecordEntity::getStatus, status)
			.set(errorCount != null , DmpOutputTaskRecordEntity::getErrorCount, errorCount)
			.set(StringUtils.isNotBlank(responseData) , DmpOutputTaskRecordEntity::getResponseData, responseData)
			.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
			.update();
		if(status.equals(DmpOutputTaskRecordStatusEnum.ERROR.getCode())) {
			WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
	        warnMsgInfo.setBizName("中台推送erp");
	        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
	        warnMsgInfo.setTitle("中台推送erp失败，id=" + id);
	        warnMsgInfo.setTableName("dmp_output_task_record");
	        warnMsgInfo.setTableId(id);
	        warnMsgInfo.setKeyInfo(responseData);
	        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
	        mqProducerService.sendWarnMsg(warnMsgInfo);
	        
	        Map<String, Object> bodyMap = new HashMap<String, Object>();
			bodyMap.put("msg_type", "text");
			Map<String, String> contentMap = new HashMap<String, String>();
			
			contentMap.put("text", "中台【"+ namespace +"】环境告警：" + "输出任务记录id=【" + id + "】，单据编号=【" + code + "】处理失败：" + message);
			bodyMap.put("content", contentMap);
			HttpUtil.post("https://open.feishu.cn/open-apis/bot/v2/hook/c76b72f8-0bf9-4967-a9ce-0728767c1ccc", JSON.toJSONString(bodyMap));
		}
		return update;
	}
}
