package com.erp.server.dmp.inout.job;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputTaskFactory;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;

@Component
public class DmpInputTaskJob {
	@Autowired
	private DmpInputTaskFactory dmpInputTaskFactory;
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	
	@XxlJob("doInputTask")
    public ReturnT doInputTask(){
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery()
				.in(DmpInputTaskEntity::getStatus, Arrays.asList(DmpInputTaskStatusEnum.INIT.getCode() 
						, DmpInputTaskStatusEnum.FDS.getCode() , DmpInputTaskStatusEnum.MONGO.getCode()
						, DmpInputTaskStatusEnum.DMP.getCode()))
				.select(DmpInputTaskEntity::getId)
				.orderByDesc(DmpInputTaskEntity::getUpdateTime)
				.last(" limit 1000 ")
				.list();
		for(DmpInputTaskEntity l : list) {
			DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
			dmpInputFinishRequest.setInputTaskId(l.getId());
			dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest);
		}
        return ReturnT.SUCCESS;
    }
	
}
