package com.erp.server.dmp.inout.handler.input.task.mongo;

import cn.hutool.core.collection.CollUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputFileMongoRelationEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskFileParseStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputMongoRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * dmp输入任务mongo基础处理器，被mongo任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
@Slf4j
public class DmpInputFsMongoHandler extends DmpInputBaseMongoHandler{

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
		mongoStorageName = DmpHandlerUtils.getMongoStorageName(dmpBasicSystemEntity, dmpCfgInputEntity, dmpCfgInputConvertEntity);
		allFieldFlag = DmpHandlerUtils.getAllFieldFlag(dmpCfgInputConvertEntity.getUniqueFieldName(), uniqueFieldSet);
		updateTaskStatus = DmpInputTaskStatusEnum.MONGO;
		this.beforeToDoStatus(dmpRequest, dmpResponse);

		List<Map<String, Object>> dmpInputMongoBaseEntityList = null;
		Integer count = dmpInputTaskFileService.lambdaQuery()
				.eq(DmpInputTaskFileEntity::getMainId, inputTaskId)
				.eq(DmpInputTaskFileEntity::getParseStatus, DmpInputTaskFileParseStatusEnum.WAIT.getCode())
				.count();
		if(count == null || count == 0) {
			List<ParamData> paramDataList = new ArrayList<>();
			paramDataList.add(new ParamData(MONGO_BASE_INPUTTASKID, MONGO_BASE_INPUTTASKID, PannoEnum.EQ, inputTaskId));
			paramDataList.add(new ParamData(MONGO_BASE_CONVERTID, MONGO_BASE_CONVERTID, PannoEnum.EQ, convertId));
			dmpInputMongoBaseEntityList = mongoService.findMongoData(paramDataList, mongoStorageName);
			this.isNextStatus(dmpResponse);
		}else {
			this.dealConvertInputTaskFileEntityListMaps(dmpResponse);
			Map<DmpCfgInputConvertEntity, List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = dmpResponse.getConvertInputTaskFileEntityListMaps();
			if(convertInputTaskFileEntityListMaps != null && convertInputTaskFileEntityListMaps.size() > 0) {
				dmpInputMongoBaseEntityList = this.parseFdsToMongo(dmpRequest,  dmpResponse);
			}else {
				Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertIdInputTaskInitDTOListMaps = dmpResponse.getConvertInputTaskInitDTOListMaps();
				if(convertIdInputTaskInitDTOListMaps != null && convertIdInputTaskInitDTOListMaps.size() > 0) {
					dmpInputMongoBaseEntityList = this.parseInitToMongo(dmpRequest, dmpResponse);
				}else {
					dmpInputMongoBaseEntityList = this.parseNoneToMongo(dmpRequest, dmpResponse);
				}
			}
			dmpInputMongoBaseEntityList = this.parseToMongo(dmpInputMongoBaseEntityList);
		}

		dmpResponse.getConvertInputMongoEntityListMaps().put(dmpCfgInputConvertEntity, dmpInputMongoBaseEntityList);
		dmpResponse.getChangeConvertInputMongoEntityListMaps().put(dmpCfgInputConvertEntity, changeConvertInputMongoEntityList);
		this.afterToDoStatus(dmpRequest, dmpResponse);

		DmpOutputTaskRequest dmpOutputMongoRequest = new DmpOutputTaskRequest();
		dmpOutputMongoRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
		dmpOutputMongoRequest.setConvertInputTaskFileEntityListMaps(dmpResponse.getConvertInputTaskFileEntityListMaps());
		dmpOutputMongoRequest.setConvertInputMongoEntityListMaps(dmpResponse.getConvertInputMongoEntityListMaps());
		dmpOutputMongoRequest.setChangeConvertInputMongoEntityListMaps(dmpResponse.getChangeConvertInputMongoEntityListMaps());
		this.doBaseChain(dmpRequest, dmpResponse, chain, dmpOutputMongoRequest);
	}

	protected List<Map<String, Object>> parseToMongo(List<Map<String, Object>> dmpInputMongoEntityList){
		Map<String, Map<String, Object>> md5DmpInputMongoEntityMaps = dmpInputMongoEntityList.stream().collect(Collectors.toMap(d -> d.get(MONGO_BASE_UNIQUEENCRYPT).toString(), d -> d , getBinaryOperator()));

		List<Map<String, Object>> saveDmpInputMongoEntityList = new ArrayList<>();
		if(md5DmpInputMongoEntityMaps.size() > 0) {
			List<Map<String, Object>> updateDmpInputMongoEntityList = new ArrayList<>();
			this.compareData(saveDmpInputMongoEntityList, updateDmpInputMongoEntityList, md5DmpInputMongoEntityMaps);
			if(CollUtil.isNotEmpty(saveDmpInputMongoEntityList)) {
				mongoService.saveMongoDataMult(saveDmpInputMongoEntityList, mongoStorageName);
			}
			if(CollUtil.isNotEmpty(updateDmpInputMongoEntityList)) {
				mongoService.upsertMongoDataBatch(updateDmpInputMongoEntityList, mongoStorageName);
				saveDmpInputMongoEntityList.addAll(updateDmpInputMongoEntityList);
			}
		}

		if(CollUtil.isNotEmpty(saveDmpInputMongoEntityList)) {
			List<DmpInputFileMongoRelationEntity> dmpInputFileMongoRelationEntityList = new ArrayList<>(saveDmpInputMongoEntityList.size());
			DmpInputFileMongoRelationEntity dmpInputFileMongoRelationEntity = null;
			for(Map<String , Object> allDmpInputMongoEntity : saveDmpInputMongoEntityList) {
				dmpInputFileMongoRelationEntity = new DmpInputFileMongoRelationEntity();
				dmpInputFileMongoRelationEntity.setMongoId(allDmpInputMongoEntity.get(MONGO_BASE_ID).toString());
				dmpInputFileMongoRelationEntity.setFileId(allDmpInputMongoEntity.get(MONGO_BASE_FILEID).toString());
				dmpInputFileMongoRelationEntity.setConvertId(convertId);

				dmpInputFileMongoRelationEntityList.add(dmpInputFileMongoRelationEntity);
			}
			dmpInputFileMongoRelationService.saveBatch(dmpInputFileMongoRelationEntityList);
		}

		return saveDmpInputMongoEntityList;
	}


	protected BinaryOperator<Map<String, Object>> getBinaryOperator(){
		return (d1 , d2) -> d1.get("startTime").toString().compareTo(d1.get("startTime").toString()) > 1 ? d1 : d2;
	}
}
