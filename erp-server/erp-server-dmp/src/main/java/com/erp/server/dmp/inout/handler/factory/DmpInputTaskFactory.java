package com.erp.server.dmp.inout.handler.factory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import cn.hutool.core.exceptions.ExceptionUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.input.all.DmpInputTaskStatusHandler;
import com.erp.server.dmp.inout.handler.input.task.DmpInputBaseTaskHandler;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 输入任务工厂，添加handler给handler链路执行
 * @author Administrator
 *
 */
@Slf4j
@Component
public class DmpInputTaskFactory{
	
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	@Autowired
	private DmpInputTaskStatusHandler dmpInputTaskStatusHandler;
	@Resource
    private RedisTemplate<String,Object> redisTemplate;
	@Autowired
	private DmpInputTaskFactory dmpInputTaskFactory;
	
	/**
	 * 执行输入任务
	 * @param dmpInputFinishRequest
	 * @return
	 */
	public DmpInputFinishResponse dealInputTask(DmpInputFinishRequest dmpInputFinishRequest) {
		String inputTaskId = dmpInputFinishRequest.getInputTaskId();
		
		if(StringUtils.isBlank(inputTaskId)) {
			log.warn("输入任务id为空");
			return null;
		}
		DmpInputTaskEntity dbDmpInputTaskEntity = dmpInputTaskService.getById(inputTaskId);
		if(dbDmpInputTaskEntity == null) {
			log.warn("输入任务不存在id={}" , inputTaskId);
			return null;
		}
		
		List<DmpInputTaskStatusEnum> values = DmpInputTaskStatusEnum.getNextStatus(dbDmpInputTaskEntity.getStatus());
		DmpInputFinishResponse dmpResponse = new DmpInputFinishResponse();
		
		String redisKey = "dmp:input:task:" + inputTaskId;
		Integer execTimeout = dmpInputFinishRequest.getExecTimeout();
		if(execTimeout == null) {
			execTimeout = dbDmpInputTaskEntity.getExecTimeout();
		}
		if(execTimeout == null || execTimeout < 3) {
			execTimeout = 3600;
		}
		if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), execTimeout, TimeUnit.SECONDS)) {
			try {
				for(DmpInputTaskStatusEnum value : values) {
					if(value != DmpInputTaskStatusEnum.INIT && value != DmpInputTaskStatusEnum.ERROR && dmpResponse.isDoNextStatus()) {
						dmpInputTaskFactory.innerDealInputTask(value, dmpInputFinishRequest, dmpResponse);
					}
				}
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
	
	@Transactional(rollbackFor = Exception.class)
	public void innerDealInputTask(DmpInputTaskStatusEnum value , DmpInputFinishRequest dmpInputFinishRequest , DmpInputFinishResponse dmpResponse) {
		String inputTaskId = dmpInputFinishRequest.getInputTaskId();
		String code = value.getCode();
		log.info("{}任务开始执行，执行状态{}" , inputTaskId , code);
		try {
			dmpInputFinishRequest.setDealTaskStatus(value);
			DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
			bean.addDmpHandler(ApplicationContextUtils.getBean(DmpInputBaseTaskHandler.class));
			bean.addDmpHandler(dmpInputTaskStatusHandler);
			bean.doDmpHandler(dmpInputFinishRequest, dmpResponse);
		} catch (Exception e) {
			log.error("{}任务执行报错，执行状态{}, 异常类型={}" , inputTaskId , code, ExceptionUtil.stacktraceToString(e));
			Integer maxRetryCount = 3;
			DmpCfgInputDetailEntity dmpCfgInputDetailEntity = dmpResponse.getDmpCfgInputDetailEntity();
			if(dmpCfgInputDetailEntity != null) {
				maxRetryCount = dmpCfgInputDetailEntity.getMaxRetryCount();
			}
			List<DmpInputTaskEntity> beforeDmpInputTaskEntityList = dmpResponse.getBeforeDmpInputTaskEntityList();
			DmpInputTaskEntity dmpInputTaskEntity = null;
			if(CollUtil.isNotEmpty(beforeDmpInputTaskEntityList)) {
				dmpInputTaskEntity = beforeDmpInputTaskEntityList.get(0);
			}else {
				dmpInputTaskEntity = dmpInputTaskService.getById(inputTaskId);
			}
			Integer errorCount = dmpInputTaskEntity.getErrorCount() + 1;
			boolean errorFlag = errorCount.equals(maxRetryCount);
			if(maxRetryCount < 0) {
				errorFlag = false;
			}
			dmpInputTaskService.updateErrorStatus(dmpInputTaskEntity.getId(), errorFlag, errorCount, e);
			throw e;
		}
		log.info("{}任务结束执行，执行状态{}" , inputTaskId , code);
	}
}
