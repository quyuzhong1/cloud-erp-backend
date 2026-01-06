package com.erp.server.dmp.inout.handler.output.task.mq;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpCfgOutputTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

@Service
@Scope("prototype")
public abstract class DmpOutputRocketMQTaskHandler extends DmpOutputTaskHandler{
	
	@Override
	public List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpResponse.getDmpCfgOutputEntity();
		if(!dmpCfgOutputEntity.getType().equals(DmpCfgOutputTypeEnum.MQ.getCode())) {
			throw new ServiceException("非MQ输出类型，请勿配置DmpOutputRocketMQTaskHandler");
		}
		Map<String, String> pushJsonDataMap = this.getPushJsonDataMap(dmpRequest, dmpResponse);
		
		List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
		
		List<String> sourceCodeKeys = this.getSourceCodeKeys();
		for(Map.Entry<String, String> pushJsonData : pushJsonDataMap.entrySet()) {
			DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
			String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
			dmpOutputTaskRecordEntity.setId(id);
			dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
			String key = pushJsonData.getKey();
			dmpOutputTaskRecordEntity.setDataId(key);
			String value = pushJsonData.getValue();
			JSONObject parseObject = JSON.parseObject(value);
			parseObject.put("dmpOutputTaskRecordId", id);
			parseObject.put("dmpOutputTaskRecordDataId", key);
			if(CollUtil.isNotEmpty(sourceCodeKeys)) {
				dmpOutputTaskRecordEntity.setSourceCode(sourceCodeKeys.stream().map(s -> {
					String string = parseObject.getString(s);
					if(string == null) {
						string = "";
					}
					return string;
				}).collect(Collectors.joining("_")));
			}
			dmpOutputTaskRecordEntity.setRequestData(JSON.toJSONString(parseObject));
			dmpOutputTaskRecordEntity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
			dmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
		}
		
		return dmpOutputTaskRecordEntityList;
	}
	
	@Override
	public void pushData(DmpCfgOutputEntity dmpCfgOutputEntity , DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
		String typeId = dmpCfgOutputEntity.getTypeId();
		String cfgOutputId = dmpCfgOutputEntity.getId();
		String id = dmpOutputTaskRecordEntity.getId();
		
		boolean pushLastDataFlag = false;
		String extendJson = dmpCfgOutputEntity.getExtendJson();
		if(StringUtils.isNotBlank(extendJson)) {
			JSONObject parseObject = JSON.parseObject(extendJson);
			if(parseObject != null) {
				Boolean pushLastDataFlagValue = parseObject.getBoolean("pushLastDataFlag");
				if(pushLastDataFlagValue != null && pushLastDataFlagValue) {
					pushLastDataFlag = true;
				}
			}
		}
		
		String lastSql = " and exists (select 1 from dmp_output_task g where g.id = dmp_output_task_record.main_id and g.cfg_output_id = '"+ cfgOutputId +"' ) ";
		
		if(pushLastDataFlag) {
			dmpOutputTaskRecordService.lambdaUpdate()
			.eq(DmpOutputTaskRecordEntity::getDataId, dmpOutputTaskRecordEntity.getDataId())
			.ne(DmpOutputTaskRecordEntity::getId, id)
			.le(DmpOutputTaskRecordEntity::getCreateTime, dmpOutputTaskRecordEntity.getCreateTime())
			.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
			.last(lastSql)
			.set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
			.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
			.setSql(" response_data = concat('配置同一dataid，推送最新的记录id="+ id +"' , response_data) ")
			.update();
		}else {
			Integer count = dmpOutputTaskRecordService.lambdaQuery()
					.eq(DmpOutputTaskRecordEntity::getDataId, dmpOutputTaskRecordEntity.getDataId())
					.ne(DmpOutputTaskRecordEntity::getId, dmpOutputTaskRecordEntity.getId())
					.le(DmpOutputTaskRecordEntity::getCreateTime, dmpOutputTaskRecordEntity.getCreateTime())
					.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
					.last(lastSql)
					.count();
			if(count != null && count > 0) {
				dmpOutputTaskRecordService.lambdaUpdate()
					.set(DmpOutputTaskRecordEntity::getResponseData, "单据上一步操作未推送成功，同一dataId")
					.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
					.eq(DmpOutputTaskRecordEntity::getId, dmpOutputTaskRecordEntity.getId())
					.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
					.last(lastSql)
					.update();
				return;
			}
		}
		
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
			dmpOutputUtils.updateStatus(id, status, responseData , "发送RocketMQ数据异常");
		} catch (Exception e) {
			dmpOutputUtils.updateStatus(id, DmpOutputTaskRecordStatusEnum.MQERROR.getCode(), "发送RocketMQ前失败" + ExceptionUtil.stacktraceToOneLineString(e) , "发送RocketMQ前失败");
		}
	}
	
	public abstract Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse);
	
}
