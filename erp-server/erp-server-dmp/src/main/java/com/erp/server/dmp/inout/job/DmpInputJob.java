package com.erp.server.dmp.inout.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;

@Component
public class DmpInputJob {
	@Autowired
	private DmpInputCreateFactory dmpInputCreateFactory;
	
	@XxlJob("dmpInputNormalCreateTask")
    public ReturnT dmpInputNormalCreateTask(){
		DmpInputCreateRequest dmpRequest = new DmpInputCreateRequest();
		dmpRequest.setCfgInputId("");
		dmpInputCreateFactory.createNormalInputTask(dmpRequest);
        return ReturnT.SUCCESS;
    }
}
