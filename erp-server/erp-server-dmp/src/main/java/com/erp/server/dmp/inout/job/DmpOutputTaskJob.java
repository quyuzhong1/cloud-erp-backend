package com.erp.server.dmp.inout.job;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.controller.api.DmpInoutController;
import com.erp.server.dmp.inout.dto.request.DmpOutputFinishRequest;
import com.erp.server.dmp.inout.handler.factory.DmpOutputTaskFactory;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpOutputTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;

@Component
public class DmpOutputTaskJob {
	@Autowired
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	@Autowired
	private DmpOutputTaskService dmpOutputTaskService;
	@Autowired
	private DmpCfgOutputService dmpCfgOutputService;
    @Resource
	private DmpOutputTaskFactory dmpOutputTaskFactory;
    @Autowired
	private DmpInoutController dmpInoutController;
    @Autowired
	@Qualifier("dmpDoOutputErrorTask")
	private ExecutorService dmpDoOutputErrorTask;
	
    private List<String> systemIds = new ArrayList<>();
	
	@XxlJob("doOutputErrorTask")
    public ReturnT doOutputErrorTask(){
		if(CollUtil.isEmpty(systemIds)) {
			systemIds = dmpCfgOutputService.list().stream().map(DmpCfgOutputEntity::getSystemId).distinct().collect(Collectors.toList());
		}
		String jobParam = XxlJobHelper.getJobParam();
		String size = "150";
		if(StringUtils.isNotBlank(jobParam)) {
			JSONObject parseObject = JSON.parseObject(jobParam);
			String sizeParam = parseObject.getString("size");
			if(StringUtils.isNotBlank(sizeParam)) {
				size = sizeParam;
			}
		}
		
		String finalSize = size;
		for(String systemId : systemIds) {
			dmpDoOutputErrorTask.execute(() -> {
				List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpOutputTaskRecordService.getOutputErrorTask(systemId, finalSize);
				if(CollUtil.isNotEmpty(dmpOutputTaskRecordEntityList)) {
					dmpOutputTaskRecordService.batchSync(dmpOutputTaskRecordEntityList);
				}
			});
		}
		
        return ReturnT.SUCCESS;
    }

	/**
	 * 重推error状态数据
	 * @return
	 */
	@XxlJob("retryOutputErrorTask")
    public ReturnT retryOutputErrorTask(){
		long offset = 8;
		String jobParam = XxlJobHelper.getJobParam();
		if(StringUtils.isNotBlank(jobParam)) {
			offset = Long.parseLong(jobParam);
		}
		LocalDateTime updateTime = LocalDateTimeUtil.offset(LocalDateTime.now(), offset*-1, ChronoUnit.HOURS);
		dmpOutputTaskRecordService.lambdaUpdate()
			.eq(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.ERROR.getCode())
			.le(DmpOutputTaskRecordEntity::getUpdateTime, updateTime)
			.set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.INIT.getCode())
			.update();
		return ReturnT.SUCCESS;
	}

	/**
	 * 输出任务执行
	 */
	@XxlJob("doOutputTask")
	public ReturnT<String> doOutputTask(){
		String idList = XxlJobHelper.getJobParam();
		if(StringUtils.isNotBlank(idList)) {
			String[] ids = idList.split(",");
			for(String id : ids) {
				DmpOutputFinishRequest dmpOutputFinishRequest = new DmpOutputFinishRequest();
				dmpOutputFinishRequest.setOutputTaskId(id);
				dmpOutputTaskFactory.dealOutputTask(dmpOutputFinishRequest);
			}
		}
		return ReturnT.SUCCESS;
	}
	
	@XxlJob("wdtInsufficientInventoryTask")
    public ReturnT wdtInsufficientInventoryTask(){
		dmpInoutController.getWdtInsufficientInventory();
		return ReturnT.SUCCESS;
	}
}
