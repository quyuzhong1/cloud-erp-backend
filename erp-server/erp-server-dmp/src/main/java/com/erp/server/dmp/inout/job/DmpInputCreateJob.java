package com.erp.server.dmp.inout.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

@Component
public class DmpInputCreateJob {
	@Autowired
	private DmpInputCreateFactory dmpInputCreateFactory;
	
	@XxlJob("createNormalInputTask")
    public ReturnT createNormalInputTask(){
		String cfgInputId = XxlJobHelper.getJobParam();
		DmpInputCreateRequest dmpRequest = new DmpInputCreateRequest();
		dmpRequest.setCfgInputId(cfgInputId);
		dmpInputCreateFactory.createNormalInputTask(dmpRequest);
        return ReturnT.SUCCESS;
    }
	
	@XxlJob("createHistoryInputTask")
    public ReturnT createHistoryInputTask(){
		String cfgInputId = XxlJobHelper.getJobParam();
		DmpInputCreateRequest dmpRequest = new DmpInputCreateRequest();
		dmpRequest.setCfgInputId(cfgInputId);
		dmpInputCreateFactory.createHistoryInputTask(dmpRequest);
        return ReturnT.SUCCESS;
    }
	
	@XxlJob("createHotfixInputTask")
    public ReturnT createHotfixInputTask(){
		dmpInputCreateFactory.createHotfixInputTask(JSON.parseObject(XxlJobHelper.getJobParam() , DmpInputHotfixCreateRequest.class));
        return ReturnT.SUCCESS;
    }
	
	@XxlJob("doHotfixInputTask")
	public ReturnT doHotfixInputTask(){
		dmpInputCreateFactory.doHotfixInputTask(JSON.parseObject(XxlJobHelper.getJobParam() , DmpInputHotfixCreateRequest.class));
		return ReturnT.SUCCESS;
	}
}
