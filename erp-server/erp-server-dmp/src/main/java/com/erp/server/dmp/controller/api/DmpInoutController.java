package com.erp.server.dmp.controller.api;


import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;


/**
 * 输入输出任务
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/dmpInout")
public class DmpInoutController extends BaseController {

	@Autowired
	private DmpInputCreateFactory dmpInputCreateFactory;
	
    @GetMapping("doInputTask")
    public ApiResult<?> doInputTask(String cfgInputId) {
    	DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
    	dmpInputHotfixCreateRequest.setCfgInputId(cfgInputId);
    	dmpInputHotfixCreateRequest.setStartTime(LocalDateTime.now().minusHours(6));
    	dmpInputHotfixCreateRequest.setEndTime(LocalDateTime.now());
    	return success(dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest));
    }
    
}
