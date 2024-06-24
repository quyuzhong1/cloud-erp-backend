package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputBaseMongoHandler;

@Service
@Scope("prototype")
public class DmpInputBaseDmpHandler extends DmpInputDmpHandler{

	@Autowired
	private DmpInputBaseMongoHandler dmpInputBaseMongoHandler;
	
	@Override
	public List<Map<String, Object>> convertMongoToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputMongoResponse dmpResponse) {
		List<Map<String, Object>> dataList = new ArrayList<>();
		Collection<List<Map<String, Object>>> values = dmpResponse.getConvertInputMongoEntityListMaps().values();
		for(List<Map<String, Object>> v : values) {
			dataList.addAll(v);
		}
		return dataList;
	}

	@Override
	public List<Map<String, Object>> convertFdsToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputFdsResponse dmpResponse) {
		return dmpInputBaseMongoHandler.parseFdsToMongo(dmpRequest, dmpResponse);
	}

	@Override
	public List<Map<String, Object>> convertInitToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputInitResponse dmpResponse) {
		return dmpInputBaseMongoHandler.parseInitToMongo(dmpRequest, dmpResponse);
	}

	@Override
	public List<Map<String, Object>> convertNoneToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		return new ArrayList<>();
	}

}
