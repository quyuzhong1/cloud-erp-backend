package com.erp.server.dmp.inout.handler.factory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpCfgOutputTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpOutputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 输出任务工厂，添加handler给handler链路执行
 * @author Administrator
 *
 */
@Slf4j
@Component
public class DmpOutputTaskFactory{
	
	@Autowired
	private DmpOutputTaskService dmpOutputTaskService;
	
	@Autowired
	private DmpCfgOutputService dmpCfgOutputService;
	
	@Resource
    private RedisTemplate<String,Object> redisTemplate;
	
	/**
	 * 执行输出任务
	 * @param dmpInputFinishRequest
	 * @return
	 */
	public DmpOutputTaskResponse dealOutputTask(DmpOutputTaskRequest dmpOutputTaskRequest) {
		String outputTaskId = dmpOutputTaskRequest.getOutputTaskId();
		
		if(StringUtils.isBlank(outputTaskId)) {
			log.warn("输出任务id为空");
			return null;
		}
		DmpOutputTaskEntity dbDmpOutputTaskEntity = dmpOutputTaskService.getById(outputTaskId);
		if(dbDmpOutputTaskEntity == null) {
			log.warn("输出任务不存在id={}" , outputTaskId);
			return null;
		}
		
		DmpOutputTaskResponse dmpResponse = new DmpOutputTaskResponse();
		dmpResponse.setBeforeDmpOutputTaskEntityList(Collections.singletonList(dbDmpOutputTaskEntity));
		
		String redisKey = "dmp:output:task:" + outputTaskId;
		Integer execTimeout = dmpOutputTaskRequest.getExecTimeout();
		if(execTimeout == null) {
			execTimeout = dbDmpOutputTaskEntity.getExecTimeout();
		}
		if(execTimeout == null || execTimeout < 3) {
			execTimeout = 3600;
		}
		if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), execTimeout, TimeUnit.SECONDS)) {
			try {
				log.info("输出{}任务开始执行" , outputTaskId);
				try {
					DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
					String cfgOutputId = dbDmpOutputTaskEntity.getCfgOutputId();
					DmpCfgOutputEntity dmpCfgOutputEntity = dmpCfgOutputService.getById(cfgOutputId);
					dmpResponse.setDmpCfgOutputEntity(dmpCfgOutputEntity);
					String outputClass = dmpCfgOutputEntity.getOutputClass();
					bean.addDmpHandler(ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(outputClass) , DmpHandler.class));
					bean.doDmpHandler(dmpOutputTaskRequest, dmpResponse);
				} catch (Exception e) {
					log.error("输出{}任务执行报错" , outputTaskId , e);
					Integer maxRetryCount = 3;
					DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity = dmpResponse.getDmpCfgOutputDetailEntity();
					if(dmpCfgOutputDetailEntity != null) {
						maxRetryCount = dmpCfgOutputDetailEntity.getMaxRetryCount();
					}
					List<DmpOutputTaskEntity> beforeDmpOutputTaskEntityList = dmpResponse.getBeforeDmpOutputTaskEntityList();
					if(CollUtil.isNotEmpty(beforeDmpOutputTaskEntityList)) {
						DmpOutputTaskEntity dmpInputTaskEntity = beforeDmpOutputTaskEntityList.get(0);
						Integer errorCount = dmpInputTaskEntity.getErrorCount() + 1;
						boolean errorFlag = errorCount.equals(maxRetryCount);
						dmpOutputTaskService.updateErrorStatus(dmpInputTaskEntity.getId(), errorFlag, errorCount, e);
					}
					throw e;
				}
				log.info("输出{}任务结束执行" , outputTaskId);
			}catch (Exception e) {
				throw e;
			}finally {
				redisTemplate.delete(redisKey);
			}
		}else {
			log.error(redisKey + "任务正在执行中");
		}
		return dmpResponse;
	}
	
}
