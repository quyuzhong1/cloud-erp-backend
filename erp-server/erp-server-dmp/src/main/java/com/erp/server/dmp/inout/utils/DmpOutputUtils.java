package com.erp.server.dmp.inout.utils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.utils.RedisUtil;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpOutputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmpOutputUtils{
	
	@Autowired
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	@Autowired
	private DmpOutputTaskService dmpOutputTaskService;
	@Autowired
	private DmpHandlerCache dmpHandlerCache;
	@Resource
    private MQProducerService mqProducerService;
	@Resource
	private RedisUtil redisUtil;
	
	public boolean updateStatus(String id , String status , String responseData , String message) {
		Integer errorCount = null;
		String code = "";
		String systemName = "";
		if(responseData == null) {
			responseData = "";
		}
		if(status.contains(DmpOutputTaskRecordStatusEnum.ERROR.getCode())) {
			DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = dmpOutputTaskRecordService.getById(id);
			errorCount = dmpOutputTaskRecordEntity.getErrorCount();
			code = dmpOutputTaskRecordEntity.getSourceCode();
			String mainId = dmpOutputTaskRecordEntity.getMainId();
			DmpOutputTaskEntity dmpOutputTaskEntity = dmpOutputTaskService.getById(mainId);
			List<DmpCfgOutputEntity> dmpCfgOutputEntityList = dmpHandlerCache.getDmpCfgOutputEntityList(d -> d.getId().equals(dmpOutputTaskEntity.getCfgOutputId()));
			if(CollUtil.isNotEmpty(dmpCfgOutputEntityList)) {
				List<DmpBasicSystemEntity> dmpBasicSystemEntityList = dmpHandlerCache.getDmpBasicSystemEntityList(d -> d.getId().equals(dmpCfgOutputEntityList.get(0).getSystemId()));
				if(CollUtil.isNotEmpty(dmpBasicSystemEntityList)) {
					systemName = dmpBasicSystemEntityList.get(0).getName();
				}
			}
			if(!(responseData.contains("数据已被他人锁住，为避免数据错误，请稍后再试") || responseData.contains("获取锁失败"))) {
				errorCount = errorCount + 1;
				if(errorCount >= 3 && errorCount%3 == 0) {
					status = DmpOutputTaskRecordStatusEnum.ERROR.getCode();
				}
			}
		}
		boolean update = dmpOutputTaskRecordService.lambdaUpdate()
			.eq(DmpOutputTaskRecordEntity::getId, id)
			.ne(!"金蝶".equals(systemName), DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
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
//	        mqProducerService.sendWarnMsg(warnMsgInfo);
	        
	        if(StringUtils.isBlank(message)) {
	        	message = responseData;
	        }
	        
	        boolean isSend = true;
	        if(StringUtils.isNotBlank(message) && message.contains("旺店通出库") && message.contains("msg=库存不足")) {
	        	String redisKey = "wdt:error:code:" + code;
	        	Object object = redisUtil.get(redisKey);
	        	
	        	if(object == null) {
	        		redisUtil.set(redisKey, DateUtil.now() , 86400);
	        		isSend = false;
	        	}else {
	        		boolean isAfter = DateTime.now().after(DateUtil.offsetHour(DateUtil.parse(object.toString()), 2));
	        		if(isAfter) {
	        			redisUtil.del(redisKey);
	        		}else {
	        			isSend = false;
	        		}
	        	}
	        }
	        if(errorCount != null && errorCount > 3) {
	        	isSend = false;
	        }
	        if(isSend) {
	        	DmpHandlerUtils.sendFeiShuMsg("输出【" + systemName +"】任务记录id=【" + id + "】，单据编号=【" + code + "】处理失败：" + message);
	        }
		}
		return update;
	}
	
	public void outputErrorCountMsg() {
		WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName("预警消息");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfo.setTitle("今天之前中台各业务推送失败数量汇总");
        warnMsgInfo.setTableName("dmp_output_task_record");
        warnMsgInfo.setTableId("无");
        List<String> outputErrorCountMsg = dmpOutputTaskRecordService.outputErrorCountMsg();
        if(CollUtil.isEmpty(outputErrorCountMsg)) {
        	warnMsgInfo.setKeyInfo("无推送失败数据");
        }else {
        	warnMsgInfo.setKeyInfo(outputErrorCountMsg.stream().collect(Collectors.joining("\n")));
        }
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
	}
}
