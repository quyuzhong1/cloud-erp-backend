package com.erp.server.dmp.inout.job;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputTaskFactory;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

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
		String size = XxlJobHelper.getJobParam();
		if(StringUtils.isBlank(size)) {
			size = "1000";
		}
		this.doInputTask(size, DmpInputTaskTaskTypeEnum.NORMAL);
        return ReturnT.SUCCESS;
    }
	
	@XxlJob("doInputHistoryTask")
    public ReturnT doInputHistoryTask(){
		String size = XxlJobHelper.getJobParam();
		if(StringUtils.isBlank(size)) {
			size = "1000";
		}
		this.doInputTask(size, DmpInputTaskTaskTypeEnum.HISTORY);
        return ReturnT.SUCCESS;
    }
	
	private void doInputTask(String size , DmpInputTaskTaskTypeEnum dmpInputTaskTaskTypeEnum) {
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery()
				.in(DmpInputTaskEntity::getStatus, Arrays.asList(DmpInputTaskStatusEnum.INIT.getCode() 
						, DmpInputTaskStatusEnum.FDS.getCode() , DmpInputTaskStatusEnum.MONGO.getCode()
						, DmpInputTaskStatusEnum.DMP.getCode()))
				.eq(DmpInputTaskEntity::getTaskType, dmpInputTaskTaskTypeEnum.getCode())
				.select(DmpInputTaskEntity::getId)
				.orderByDesc(DmpInputTaskEntity::getUpdateTime)
				.last(" limit " + size)
				.list();
		for(DmpInputTaskEntity l : list) {
			dmpInputExecutorPool.execute(() -> {
				DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
				dmpInputFinishRequest.setInputTaskId(l.getId());
				dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest);
			});
		}
	}
}
