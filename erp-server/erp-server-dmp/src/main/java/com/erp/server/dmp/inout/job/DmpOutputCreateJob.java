package com.erp.server.dmp.inout.job;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpOutputCreateFactory;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 创建output相关任务
 */
@Component
public class DmpOutputCreateJob {
	@Resource
	private DmpOutputCreateFactory dmpOutputCreateFactory;
	
	@Resource
	private DmpCfgOutputService dmpCfgOutputService;
	
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
