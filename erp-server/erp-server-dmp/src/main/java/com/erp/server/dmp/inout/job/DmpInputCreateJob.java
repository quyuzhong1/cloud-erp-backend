package com.erp.server.dmp.inout.job;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmpInputCreateJob {
	@Autowired
	private DmpInputCreateFactory dmpInputCreateFactory;
	
	@Autowired
	private DmpCfgInputService dmpCfgInputService;
	
	@Resource
    private RedisTemplate<String,Object> redisTemplate;
	
	/**
	 * 创建正常任务
	 * @return
	 */
	@XxlJob("createNormalInputTask")
    public ReturnT createNormalInputTask(){
		String cfgInputId = XxlJobHelper.getJobParam();
		DmpInputCreateRequest dmpRequest = new DmpInputCreateRequest();
		dmpRequest.setCfgInputId(cfgInputId);
		dmpInputCreateFactory.createNormalInputTask(dmpRequest);
        return ReturnT.SUCCESS;
    }
	
	/**
	 * 创建历史任务
	 * @return
	 */
	@XxlJob("createHistoryInputTask")
    public ReturnT createHistoryInputTask(){
		String cfgInputId = XxlJobHelper.getJobParam();
		DmpInputCreateRequest dmpRequest = new DmpInputCreateRequest();
		dmpRequest.setCfgInputId(cfgInputId);
		dmpInputCreateFactory.createHistoryInputTask(dmpRequest);
        return ReturnT.SUCCESS;
    }
	
	/**
	 * 根据系统创建正常任务和历史任务
	 * @return
	 */
	@XxlJob("createInputTaskBySystem")
	public ReturnT createInputTaskBySystem(){
		String systemId = XxlJobHelper.getJobParam();
		String redisKey = "dmp:input:create:system:" + systemId;
		
		if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 300, TimeUnit.SECONDS)) {
			try {
				List<DmpCfgInputEntity> list = dmpCfgInputService.lambdaQuery()
						.eq(DmpCfgInputEntity::getSystemId, systemId)
						.eq(DmpCfgInputEntity::getDisabled, false)
						.select(DmpCfgInputEntity::getId)
						.list();
				if(CollUtil.isNotEmpty(list)) {
					DmpInputCreateRequest dmpRequest = null;
					for(DmpCfgInputEntity l : list) {
						String id = l.getId();
						
						dmpRequest = new DmpInputCreateRequest();
						dmpRequest.setCfgInputId(id);
						dmpInputCreateFactory.createNormalInputTask(dmpRequest);
						
						dmpRequest = new DmpInputCreateRequest();
						dmpRequest.setCfgInputId(id);
						dmpInputCreateFactory.createCompensateInputTask(dmpRequest);
						
						dmpRequest = new DmpInputCreateRequest();
						dmpRequest.setCfgInputId(id);
						dmpInputCreateFactory.createHistoryInputTask(dmpRequest);
					}
				}
			} catch (Exception e) {
				log.error("输入任务生成错误systemId={}" , systemId , e);
			} finally {
				redisTemplate.delete(redisKey);
			}
		}else {
			log.error("输入任务生成正在执行中systemId={}" , systemId);
		}
		
		return ReturnT.SUCCESS;
	}
	
	/**
	 * 创建快速任务并执行
	 * @return
	 */
	@XxlJob("doHotfixInputTask")
	public ReturnT doHotfixInputTask(){
		dmpInputCreateFactory.doHotfixInputTask(JSON.parseObject(XxlJobHelper.getJobParam() , DmpInputHotfixCreateRequest.class));
		return ReturnT.SUCCESS;
	}
}
