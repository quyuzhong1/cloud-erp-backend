package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputMongoRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;
import com.erp.server.dmp.pull.mongo.MongoService;

import cn.hutool.core.collection.CollUtil;

@Service
public abstract class DmpInputMongoHandler extends DmpInputTaskHandler{
	
	protected String mongoStorageName;
	
	protected static final String MONGO_BASE_ID = "id";
	protected static final String MONGO_BASE_INPUTTASKID = "inputTaskId";
	protected static final String MONGO_BASE_FILEID = "fileId";
	protected static final String MONGO_BASE_CONVERTID = "convertId";
	protected static final String MONGO_BASE_ROWNUMBER = "rowNumber";
	protected static final String MONGO_BASE_UNIQUEFIELDMD5 = "uniqueFieldMd5";
	
	@Autowired
	protected MongoService mongoService;
	
	@Override
	public void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputMongoRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpInputMongoResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpInputMongoRequest) dmpRequest, (DmpInputMongoResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpInputMongoRequest dmpRequest, DmpInputMongoResponse dmpResponse, DmpHandlerChain chain) {
		mongoStorageName = dmpResponse.getDmpBasicSystemEntity().getCode() + "_" + dmpResponse.getDmpCfgInputEntity().getCode() + "_" + dmpCfgInputConvertEntity.getStorageName();
		
		String inputTaskId = dmpRequest.getInputTaskId();
		Map<String, Object> fieldValueMaps = new HashMap<>();
		fieldValueMaps.put(MONGO_BASE_INPUTTASKID, inputTaskId);
		fieldValueMaps.put(MONGO_BASE_CONVERTID, dmpCfgInputConvertEntity.getId());
		List<Map> dmpInputMongoBaseEntityList = mongoService.findMongoData(fieldValueMaps, mongoStorageName);
		if(CollUtil.isNotEmpty(dmpInputMongoBaseEntityList)) {
			
		}else {
			Map<DmpCfgInputConvertEntity, List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = dmpResponse.getConvertInputTaskFileEntityListMaps();
			if(convertInputTaskFileEntityListMaps != null && convertInputTaskFileEntityListMaps.size() > 0) {
				dmpInputMongoBaseEntityList = parseFdsToMongo(dmpRequest,  dmpResponse);
			}else {
				Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertIdInputTaskInitDTOListMaps = dmpResponse.getConvertInputTaskInitDTOListMaps();
				if(convertIdInputTaskInitDTOListMaps != null && convertIdInputTaskInitDTOListMaps.size() > 0) {
					dmpInputMongoBaseEntityList = parseInitToMongo(dmpRequest, dmpResponse);
				}else {
					dmpInputMongoBaseEntityList = parseNoneToMongo(dmpRequest, dmpResponse);
				}
			}
		}
		
		dmpResponse.getConvertInputMongoEntityListMaps().put(dmpCfgInputConvertEntity, dmpInputMongoBaseEntityList);
		
		this.updateTaskStatus(DmpInputTaskStatusEnum.MONGO);
		
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	public abstract List<Map> parseFdsToMongo(DmpInputMongoRequest dmpRequest, DmpInputFdsResponse dmpResponse);
	public abstract List<Map> parseInitToMongo(DmpInputMongoRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract List<Map> parseNoneToMongo(DmpInputMongoRequest dmpRequest, DmpInputTaskResponse dmpResponse);
	
}
