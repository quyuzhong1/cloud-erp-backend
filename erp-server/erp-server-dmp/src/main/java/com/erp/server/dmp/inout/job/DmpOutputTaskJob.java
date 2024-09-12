package com.erp.server.dmp.inout.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.erp.server.dmp.inout.dto.request.DmpOutputFinishRequest;
import com.erp.server.dmp.inout.handler.factory.DmpOutputTaskFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpOutputTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import cn.hutool.core.collection.CollUtil;

import javax.annotation.Resource;

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

	@XxlJob("doOutputErrorTask")
    public ReturnT doOutputErrorTask(){
		String jobParam = XxlJobHelper.getJobParam();
		String size = "150";
		List<String> mainIds = null;
		List<String> ids = null;
		if(StringUtils.isNotBlank(jobParam)) {
			JSONObject parseObject = JSON.parseObject(jobParam);
			String sizeParam = parseObject.getString("size");
			if(StringUtils.isNotBlank(sizeParam)) {
				size = sizeParam;
			}
			String mainIdsParam = parseObject.getString("mainIds");
			if(StringUtils.isNotBlank(mainIdsParam)) {
				mainIds = Arrays.asList(mainIdsParam.split(","));
			}
			String idsParam = parseObject.getString("ids");
			if(StringUtils.isNotBlank(idsParam)) {
				ids = Arrays.asList(idsParam.split(","));
			}
		}
		
		List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpOutputTaskRecordService.lambdaQuery()
			.in(CollUtil.isNotEmpty(ids) ,DmpOutputTaskRecordEntity::getId, ids)
			.in(CollUtil.isNotEmpty(mainIds) ,DmpOutputTaskRecordEntity::getMainId, mainIds)
			.eq(DmpOutputTaskRecordEntity::getIsDeleted, false)
			.last(CollUtil.isEmpty(ids) , " and (status in ('init' , 'mqerror' , 'cosumererror') or (status = 'mqsuccess' and update_time < (CURRENT_TIMESTAMP - interval '7200 seconds'))) "
					+ "order by update_time limit " + size)
			.list();
		
		if(CollUtil.isNotEmpty(dmpOutputTaskRecordEntityList)) {
			dmpOutputTaskRecordService.batchSync(dmpOutputTaskRecordEntityList);
		}
		
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
}
