package com.erp.server.dmp.inout.handler.output.task.init;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.erp.server.dmp.inout.dto.request.DmpOutputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputInitResponse;

@Service
@Scope("prototype")
public class DmpOutputBaseInitHandler extends DmpOutputInitHandler{

	@Override
	public void test(DmpOutputInitRequest dmpRequest, DmpOutputInitResponse dmpResponse) {
		System.out.println(JSON.toJSONString(dmpRequest.getConvertInputTaskInitDTOListMaps()));
	}
	
	
}
