package com.erp.server.dmp.inout.job;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
import com.erp.model.dmp.entity.DmpOutputTaskRecordMergeEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.OutputTaskRecordMergeStatusEnum;
import com.erp.server.dmp.controller.api.DmpInoutController;
import com.erp.server.dmp.inout.dto.request.DmpOutputFinishRequest;
import com.erp.server.dmp.inout.handler.factory.DmpOutputTaskFactory;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.inout.utils.DmpOutputUtils;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpOutputTaskRecordMergeService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpOutputTaskService;
import com.google.common.collect.Lists;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DmpOutputTaskJob {
	@Autowired
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	@Autowired
	private DmpOutputTaskRecordMergeService dmpOutputTaskRecordMergeService;
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
    
    @Autowired
    private DmpHandlerCache dmpHandlerCache;
    
    @Autowired
    private DmpOutputUtils dmpOutputUtils;
	
	@XxlJob("doOutputErrorTask")
    public ReturnT doOutputErrorTask(){
		List<String> systemIds = dmpHandlerCache.getDmpCfgOutputEntityList(d -> true).stream().map(DmpCfgOutputEntity::getSystemId).distinct().collect(Collectors.toList());
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
		if(CollUtil.isNotEmpty(systemIds)) {
			for(String systemId : systemIds) {
				dmpDoOutputErrorTask.execute(() -> {
					List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpOutputTaskRecordService.getOutputErrorTask(systemId, finalSize);
					if(CollUtil.isNotEmpty(dmpOutputTaskRecordEntityList)) {
						dmpOutputTaskRecordService.batchSync(dmpOutputTaskRecordEntityList);
					}
				});
			}
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
	 * 重推error状态数据通过创建时间
	 * @return
	 */
	@XxlJob("retryOutputErrorTaskByCreateTime")
	public ReturnT retryOutputErrorTaskByCreateTime(){
		LocalDateTime createTime = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
		dmpOutputTaskRecordService.lambdaUpdate()
			.eq(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.ERROR.getCode())
			.lt(DmpOutputTaskRecordEntity::getCreateTime, createTime)
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
	
	@XxlJob("dmpMoveToHistoryTable")
	public ReturnT dmpMoveToHistoryTable(){
		JSONObject parseObject = null;
		String param = XxlJobHelper.getJobParam();
		if(StringUtils.isNotBlank(param)) {
			parseObject = JSON.parseObject(param);
		}
		Date now = new Date();
		Integer beforeDay = 60;
		Integer size = 10000;
		if(parseObject != null) {
			beforeDay = parseObject.getInteger("inputDay");
			size = parseObject.getInteger("inputSize");
		}
		log.warn("归档中台输入任务数据开始");
		try {
			dmpOutputTaskRecordService.dmpInputMoveToHistoryTable(DateUtil.formatDateTime(DateUtil.offsetDay(now, beforeDay*-1)), size.toString());
		} catch (Exception e) {
			log.error("归档中台输入任务数据失败：" , e);
			DmpHandlerUtils.sendFeiShuMsg("归档中台输入任务数据失败：" + "【" + e.getMessage() + "】");
		}
		log.warn("归档中台输入任务数据结束");
		
		beforeDay = 60;
		size = 100000;
		if(parseObject != null) {
			beforeDay = parseObject.getInteger("relationDay");
			size = parseObject.getInteger("relationSize");
		}
		log.warn("归档中台关系表数据开始");
		try {
			dmpOutputTaskRecordService.dmpRelationMoveToHistoryTable(DateUtil.formatDateTime(DateUtil.offsetDay(now, beforeDay*-1)), size.toString());
		} catch (Exception e) {
			log.error("归档中台关系表数据失败：" , e);
			DmpHandlerUtils.sendFeiShuMsg("归档中台关系表数据失败：" + "【" + e.getMessage() + "】");
		}
		log.warn("归档中台关系表数据结束");
		
		beforeDay = 60;
		size = 10000;
		if(parseObject != null) {
			beforeDay = parseObject.getInteger("outputDay");
			size = parseObject.getInteger("outputSize");
		}
		log.warn("归档中台输出任务数据开始");
		try {
			dmpOutputTaskRecordService.dmpOutputMoveToHistoryTable(DateUtil.formatDateTime(DateUtil.offsetDay(now, beforeDay*-1)), size.toString());
		} catch (Exception e) {
			log.error("归档中台输出任务数据失败：" , e);
			DmpHandlerUtils.sendFeiShuMsg("归档中台输出任务数据失败：" + "【" + e.getMessage() + "】");
		}
		log.warn("归档中台输出任务数据结束");
		
		log.warn("归档中台输出任务无记录数据开始");
		try {
			dmpOutputTaskRecordService.dmpOutputNoRecordMoveToHistoryTable();
		} catch (Exception e) {
			log.error("归档中台输出任务无记录数据失败：" , e);
			DmpHandlerUtils.sendFeiShuMsg("归档中台输出任务无记录数据失败：" + "【" + e.getMessage() + "】");
		}
		log.warn("归档中台输出任务无记录数据结束");
		
		return ReturnT.SUCCESS;
	}

	@XxlJob("sdyMergePush")
	public ReturnT sdyMergePush(){
		String jobParam = XxlJobHelper.getJobParam();
		Integer size = 60000;
		if(StringUtils.isNotBlank(jobParam)) {
			size = Integer.valueOf(jobParam);
		}
		if(size < 1000) {
			size = 1000;
		}
		if(size > 60000) {
			size = 60000;
		}
		try {
			List<DmpOutputTaskRecordMergeEntity> list = dmpOutputTaskRecordMergeService.lambdaQuery()
	                .eq(DmpOutputTaskRecordMergeEntity::getMergeStatus, OutputTaskRecordMergeStatusEnum.WAIT_MERGE.getCode())
	                .last(" order by create_time asc LIMIT " + size)
	                .list();
			if(CollUtil.isNotEmpty(list)) {
				List<List<DmpOutputTaskRecordMergeEntity>> partition = Lists.partition(list, 1000);
				for(List<DmpOutputTaskRecordMergeEntity> p : partition) {
					dmpOutputTaskRecordMergeService.sdyMergePush(p);
				}
			}
		} catch (Exception e) {
			log.error(size + "组合数据推送数帝云失败：" , e);
			DmpHandlerUtils.sendFeiShuMsg(size + "组合数据推送数帝云失败：" + "【" + e.getMessage() + "】");
		}
		return ReturnT.SUCCESS;
	}
	
	@XxlJob("outputErrorCountMsg")
	public ReturnT outputErrorCountMsg(){
		dmpOutputUtils.outputErrorCountMsg();
		return ReturnT.SUCCESS;
	}
}
