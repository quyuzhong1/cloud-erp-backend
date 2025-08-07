package com.erp.server.dmp.inout.job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputTaskFactory;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import cn.hutool.core.collection.CollUtil;

@Component
public class DmpInputTaskJob {
	@Autowired
	private DmpInputTaskFactory dmpInputTaskFactory;
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	@Autowired
	@Qualifier("dmpInputExecutorPool")
	private ExecutorService dmpInputExecutorPool;
	
	@XxlJob("doInputNormalTask")
    public ReturnT doInputNormalTask(){
		this.doInputTask(XxlJobHelper.getJobParam(), DmpInputTaskTaskTypeEnum.NORMAL);
        return ReturnT.SUCCESS;
    }
	
	@XxlJob("doInputHistoryTask")
    public ReturnT doInputHistoryTask(){
		this.doInputTask(XxlJobHelper.getJobParam(), DmpInputTaskTaskTypeEnum.HISTORY);
        return ReturnT.SUCCESS;
    }
	
	@XxlJob("doInputCompensateTask")
    public ReturnT doInputCompensateTask(){
		this.doInputTask(XxlJobHelper.getJobParam(), DmpInputTaskTaskTypeEnum.COMPENSATE);
        return ReturnT.SUCCESS;
    }
	
	private void doInputTask(String jobParam , DmpInputTaskTaskTypeEnum dmpInputTaskTaskTypeEnum) {
		String size = "1000";
		List<String> cfgInputIds = null;
		List<String> ids = null;
		
		List<String> execStatusList = Arrays.asList(DmpInputTaskStatusEnum.INIT.getCode() 
				, DmpInputTaskStatusEnum.FDS.getCode() , DmpInputTaskStatusEnum.MONGO.getCode()
				, DmpInputTaskStatusEnum.DMP.getCode());
		if(StringUtils.isNotBlank(jobParam)) {
			JSONObject parseObject = JSON.parseObject(jobParam);
			String sizeParam = parseObject.getString("size");
			if(StringUtils.isNotBlank(sizeParam)) {
				size = sizeParam;
			}
			String mainIdsParam = parseObject.getString("cfgInputIds");
			if(StringUtils.isNotBlank(mainIdsParam)) {
				cfgInputIds = Arrays.asList(mainIdsParam.split(","));
			}
			String idsParam = parseObject.getString("ids");
			if(StringUtils.isNotBlank(idsParam)) {
				ids = Arrays.asList(idsParam.split(","));
				if(CollUtil.isNotEmpty(ids)) {
					List<DmpInputTaskEntity> errorList = dmpInputTaskService.lambdaQuery()
							.in(DmpInputTaskEntity::getId, ids)
							.eq(DmpInputTaskEntity::getTaskType, dmpInputTaskTaskTypeEnum.getCode())
							.eq(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.ERROR.getCode())
							.list();
					List<DmpInputTaskEntity> updateList = new ArrayList<>();
					if(CollUtil.isNotEmpty(errorList)) {
						for(DmpInputTaskEntity e : errorList) {
							String errorMessage = e.getErrorMessage();
							if(StringUtils.isNotBlank(errorMessage)) {
								String[] split = errorMessage.split("@@");
								if(split.length > 1) {
									String status = split[0];
									if(execStatusList.contains(status)) {
										e.setErrorCount(0);
										e.setStatus(status);
										updateList.add(e);
									}
								}
							}
						}
					}
					if(CollUtil.isNotEmpty(updateList)) {
						dmpInputTaskService.updateBatchById(updateList);
					}
				}
			}
		}
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery()
				.in(DmpInputTaskEntity::getStatus, execStatusList)
				.in(CollUtil.isNotEmpty(ids) ,DmpInputTaskEntity::getId, ids)
				.in(CollUtil.isNotEmpty(cfgInputIds) ,DmpInputTaskEntity::getCfgInputId, cfgInputIds)
				.eq(DmpInputTaskEntity::getTaskType, dmpInputTaskTaskTypeEnum.getCode())
				.and(d -> d.isNull(DmpInputTaskEntity::getNextExecTime).or().le(DmpInputTaskEntity::getNextExecTime, LocalDateTime.now()))
				.select(DmpInputTaskEntity::getId , DmpInputTaskEntity::getCfgInputId , DmpInputTaskEntity::getNextLevelId , DmpInputTaskEntity::getExecTimeout)
				.orderByAsc(DmpInputTaskEntity::getUpdateTime)
				.last(dmpInputTaskTaskTypeEnum != DmpInputTaskTaskTypeEnum.COMPENSATE , " limit " + size)
				.list();
		if(dmpInputTaskTaskTypeEnum == DmpInputTaskTaskTypeEnum.COMPENSATE) {
			Map<String, List<DmpInputTaskEntity>> cfgInputNextLevelMaps = list.stream().collect(Collectors.groupingBy(l -> l.getCfgInputId() + "_" + l.getNextLevelId()));
			list = new ArrayList<>();
			for(Map.Entry<String, List<DmpInputTaskEntity>> cfgInputNextLevelMap : cfgInputNextLevelMaps.entrySet()) {
				list.add(cfgInputNextLevelMap.getValue().get(0));
			}
		}
		
		for(DmpInputTaskEntity l : list) {
			dmpInputExecutorPool.execute(() -> {
				DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
				dmpInputFinishRequest.setInputTaskId(l.getId());
				dmpInputFinishRequest.setExecTimeout(l.getExecTimeout());
				dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest);
			});
		}
	}
	
	@XxlJob("doInputTask")
    public ReturnT doInputTask(){
		String idList = XxlJobHelper.getJobParam();
		if(StringUtils.isNotBlank(idList)) {
			String[] ids = idList.split(",");
			for(String id : ids) {
				DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
				dmpInputFinishRequest.setInputTaskId(id);
				dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest);
			}
		}
        return ReturnT.SUCCESS;
    }
}
