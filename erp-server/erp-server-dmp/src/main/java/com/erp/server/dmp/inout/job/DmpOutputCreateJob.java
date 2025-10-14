package com.erp.server.dmp.inout.job;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.enums.DmpCfgInputExecSystemEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpOutputCreateFactory;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 创建output相关任务
 */
@Component
@Slf4j
public class DmpOutputCreateJob {
	@Resource
	private DmpOutputCreateFactory dmpOutputCreateFactory;
	
	@Resource
	private DmpCfgOutputService dmpCfgOutputService;
	
	@Resource
    private RedisTemplate<String,Object> redisTemplate;
	
	/**
	 * 创建正常任务
	 */
	@XxlJob("createNormalOutputTask")
    public ReturnT<String> createNormalOutputTask(){
		String cfgOutputId = XxlJobHelper.getJobParam();
		DmpOutputCreateRequest dmpRequest = new DmpOutputCreateRequest();
		dmpRequest.setCfgOutputId(cfgOutputId);
		dmpOutputCreateFactory.createNormalOutputTask(dmpRequest);
        return ReturnT.SUCCESS;
    }
	
	/**
	 * 创建历史任务
	 */
	@XxlJob("createHistoryOutputTask")
    public ReturnT<String> createHistoryOutputTask(){
		String cfgOutputId = XxlJobHelper.getJobParam();
		DmpOutputCreateRequest dmpRequest = new DmpOutputCreateRequest();
		dmpRequest.setCfgOutputId(cfgOutputId);
		dmpOutputCreateFactory.createHistoryOutputTask(dmpRequest);
        return ReturnT.SUCCESS;
    }
	
	/**
	 * 根据系统创建正常任务和历史任务
	 */
	@XxlJob("createOutputTaskBySystem")
	public ReturnT<String> createOutputTaskBySystem(){
		String systemId = XxlJobHelper.getJobParam();
		List<DmpCfgOutputEntity> list = dmpCfgOutputService.lambdaQuery()
				.eq(DmpCfgOutputEntity::getSystemId, systemId)
				.eq(DmpCfgOutputEntity::getDisabled, false)
				.eq(DmpCfgOutputEntity::getExecSystem, DmpCfgInputExecSystemEnum.DMP.getCode())
				.select(DmpCfgOutputEntity::getId)
				.list();
		if(CollUtil.isNotEmpty(list)) {
			DmpOutputCreateRequest dmpRequest = null;
			for(DmpCfgOutputEntity l : list) {
				String id = l.getId();
				
				dmpRequest = new DmpOutputCreateRequest();
				dmpRequest.setCfgOutputId(id);
				dmpOutputCreateFactory.createNormalOutputTask(dmpRequest);
				
				dmpRequest = new DmpOutputCreateRequest();
				dmpRequest.setCfgOutputId(id);
				dmpOutputCreateFactory.createHistoryOutputTask(dmpRequest);
			}
		}
		
		return ReturnT.SUCCESS;
	}
	
	@XxlJob("createOutputTaskByAppId")
	public ReturnT createEtlTaskByAppId(){
		String appId = XxlJobHelper.getJobParam();
		List<DmpCfgOutputEntity> list = dmpCfgOutputService.lambdaQuery()
				.eq(DmpCfgOutputEntity::getDisabled, false)
				.eq(DmpCfgOutputEntity::getExecSystem, DmpCfgInputExecSystemEnum.REST_CLOUD.getCode())
				.eq(DmpCfgOutputEntity::getAppId, appId)
				.list();
		if(CollUtil.isNotEmpty(list)) {
			for(DmpCfgOutputEntity l : list) {
				String flowName = l.getFlowName();
				String id = l.getId();
				String redisKey = "dmp:output:create:id:" + id;
				if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 300, TimeUnit.SECONDS)) {
					try {
						DmpOutputCreateRequest dmpRequest = new DmpOutputCreateRequest();
						dmpRequest.setCfgOutputId(id);
						dmpOutputCreateFactory.createNormalOutputTask(dmpRequest);
					} catch (Exception e) {
						log.error("output任务生成错误flowName={}" , flowName , e);
					} finally {
						redisTemplate.delete(redisKey);
					}
				}else {
					log.error("output任务生成正在执行中flowName={}" , flowName);
				}
			}
		}
		
		return ReturnT.SUCCESS;
	}
	
	/**
	 * 创建快速任务并执行
	 */
	@XxlJob("doHotfixOutputTask")
	public ReturnT<String> doHotfixOutputTask(){
		// {"cfgOutputId":""}
		dmpOutputCreateFactory.doHotfixOutputTask(JSON.parseObject(XxlJobHelper.getJobParam() , DmpOutputHotfixCreateRequest.class));
		return ReturnT.SUCCESS;
	}
}
