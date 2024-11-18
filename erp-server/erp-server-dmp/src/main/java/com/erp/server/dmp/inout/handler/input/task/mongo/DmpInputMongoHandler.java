package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputFileMongoRelationEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskFileParseStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputMongoRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpInputFileMongoRelationService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.MD5;

/**
 * dmp输入任务mongo状态处理器，被mongo任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
public abstract class DmpInputMongoHandler extends DmpInputTaskHandler{
	
	protected String mongoStorageName;
	protected Set<String> uniqueFieldSet = new HashSet<>();
	protected boolean allFieldFlag;
	protected final MD5 md5 = MD5.create();
	/**
	 * 变动的mongo业务信息
	 */
	protected List<Map<String, Object>> changeConvertInputMongoEntityList = new ArrayList<>();
	
	public static final String MONGO_BASE_ID = "_id";
	public static final String MONGO_BASE_INPUTTASKID = "inputTaskId";
	public static final String MONGO_BASE_NEXTLEVELID = "nextLevelId";
	protected static final String MONGO_BASE_FILEID = "fileId";
	protected static final String MONGO_BASE_CONVERTID = "convertId";
	protected static final String MONGO_BASE_ROWNUMBER = "rowNumber";
	protected static final String MONGO_BASE_UNIQUEENCRYPT = "uniqueEncrypt";
	protected static final String MONGO_BASE_DATAENCRYPT = "dataEncrypt";
	protected static final String MONGO_BASE_MONGOCREATETIME = "mongoCreateTime";
	public static final String MONGO_BASE_MONGOUPDATETIME = "mongoUpdateTime";
	
	public static List<String> mongoBaseFiledList = new ArrayList<>();
	static {
		mongoBaseFiledList.add(MONGO_BASE_ID);
		mongoBaseFiledList.add(MONGO_BASE_CONVERTID);
		mongoBaseFiledList.add(MONGO_BASE_INPUTTASKID);
		mongoBaseFiledList.add(MONGO_BASE_ROWNUMBER);
		mongoBaseFiledList.add(MONGO_BASE_UNIQUEENCRYPT);
		mongoBaseFiledList.add(MONGO_BASE_DATAENCRYPT);
		mongoBaseFiledList.add(MONGO_BASE_MONGOCREATETIME);
		mongoBaseFiledList.add(MONGO_BASE_MONGOUPDATETIME);
	}
	
	@Autowired
	protected DmpInputFileMongoRelationService dmpInputFileMongoRelationService;
	
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
		Map<String, Map<String, Object>> md5DmpInputMongoEntityMaps = dmpInputMongoEntityList.stream().collect(Collectors.toMap(d -> d.get(MONGO_BASE_UNIQUEENCRYPT).toString(), d -> d , (d1 , d2) -> d1));
		
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
	
	/**
	 * 设置mongo对象
	 * @param fileId
	 * @param i
	 * @param data
	 * @param allFieldFlag
	 * @param uniqueFieldSet
	 * @param md5DmpInputMongoEntityMaps
	 */
	protected List<TreeMap<String , Object>> getDmpInputMongoEntityList(DmpInputTaskFileContentTypeEnum contentType , String fileId , Integer i , Map<String, Object> data , boolean allFieldFlag , Set<String> uniqueFieldSet) {
		List<TreeMap<String , Object>> resultDataList = this.convertData(data);
		if(CollUtil.isNotEmpty(resultDataList)) {
			for(TreeMap<String , Object> dmpInputMongoEntity : resultDataList) {
				Integer rowNumber = i + 1;
				StringBuilder uniqueFieldMd5Sb = new StringBuilder();
				uniqueFieldMd5Sb.append(nextLevelId);
				StringBuilder dataMd5Sb = new StringBuilder();
				dataMd5Sb.append(nextLevelId);
				for(Map.Entry<String , Object> dmpInputMongo : dmpInputMongoEntity.entrySet()) {
					String key = dmpInputMongo.getKey();
					Object value = dmpInputMongo.getValue();
					if(value != null) {
						if(allFieldFlag || uniqueFieldSet.contains(key)) {
							uniqueFieldMd5Sb.append(value);
						}
						dataMd5Sb.append(value);
					}
				}
				if(DmpInputTaskFileContentTypeEnum.TXT == contentType || DmpInputTaskFileContentTypeEnum.CSV == contentType) {
					uniqueFieldMd5Sb.append(rowNumber);
					dataMd5Sb.append(rowNumber);
				}
				this.afterDmpInputMongoEntity(dmpInputMongoEntity, fileId , rowNumber ,uniqueFieldMd5Sb.toString(), dataMd5Sb.toString());
			}
		}
		return resultDataList;
	}
	
	/**
	 * 转换数据，可重写
	 * @param data 原始数据
	 */
	protected List<TreeMap<String , Object>> convertData(Map<String, Object> data) {
		List<TreeMap<String , Object>> resultDataList = new ArrayList<>();
		TreeMap<String , Object> dmpInputMongoEntity = new TreeMap<>();
		for(Map.Entry<String, Object> d : data.entrySet()) {
			String key = d.getKey();
			Object value = d.getValue();
			List<String> convertKey = this.convertKey(key);
			for(String c : convertKey) {
				dmpInputMongoEntity.put(c.replace(".", ""), value);
			}
		}
		resultDataList.add(dmpInputMongoEntity);
		this.afterConvertData(resultDataList);
		return resultDataList;
	}
	
	protected void afterConvertData(List<TreeMap<String , Object>> resultDataList) {
		
	}
	
	/**
	 * 设置mongo公共字段
	 * @param dmpInputMongoEntity
	 * @param fileId
	 * @param i
	 * @param uniqueFieldString
	 * @param dataString
	 */
	protected void afterDmpInputMongoEntity(Map<String , Object> dmpInputMongoEntity ,String fileId , Integer rowNumber ,  String uniqueFieldString , String dataString) {
		dmpInputMongoEntity.put(MONGO_BASE_ID, DmpHandlerUtils.getId());
		dmpInputMongoEntity.put(MONGO_BASE_INPUTTASKID , inputTaskId);
		dmpInputMongoEntity.put(MONGO_BASE_NEXTLEVELID , nextLevelId);
		dmpInputMongoEntity.put(MONGO_BASE_FILEID, fileId);
		dmpInputMongoEntity.put(MONGO_BASE_CONVERTID, convertId);
		dmpInputMongoEntity.put(MONGO_BASE_ROWNUMBER, rowNumber);
		dmpInputMongoEntity.put(MONGO_BASE_UNIQUEENCRYPT, md5.digestHex(uniqueFieldString));
		dmpInputMongoEntity.put(MONGO_BASE_DATAENCRYPT, md5.digestHex(dataString));
		String now = DateUtil.now();
		dmpInputMongoEntity.put(MONGO_BASE_MONGOCREATETIME, now);
		dmpInputMongoEntity.put(MONGO_BASE_MONGOUPDATETIME, now);
		
		this.afterDmpInputMongoEntityFixedValue(dmpInputMongoEntity);
	}
	
	protected void afterDmpInputMongoEntityFixedValue(Map<String , Object> dmpInputMongoEntity) {
		String fixedValueJson = dmpCfgInputConvertEntity.getFixedValueJson();
		if(StringUtils.isNotBlank(fixedValueJson)) {
			JSONObject fixedValueStr = JSON.parseObject(fixedValueJson);
			for(Map.Entry<String, Object> fixedValue : fixedValueStr.entrySet()) {
				dmpInputMongoEntity.put(fixedValue.getKey(), fixedValue.getValue());
			}
		}
	}
	
	/**
	 * 对比mongo数据
	 * @param saveDmpInputMongoEntityList
	 * @param updateDmpInputMongoEntityList
	 * @param md5DmpInputMongoEntityMaps
	 */
	protected void compareData(List<Map<String, Object>> saveDmpInputMongoEntityList , List<Map<String, Object>> updateDmpInputMongoEntityList , Map<String, Map<String, Object>> md5DmpInputMongoEntityMaps) {
		List<ParamData> paramDataList = new ArrayList<>();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_UNIQUEENCRYPT, DmpInputMongoHandler.MONGO_BASE_UNIQUEENCRYPT, PannoEnum.IN, new ArrayList<>(md5DmpInputMongoEntityMaps.keySet())));
		List<Map<String, Object>> dbConvertInputMongoEntityList = mongoService.findMongoData(paramDataList, mongoStorageName);
		
		if(CollUtil.isNotEmpty(dbConvertInputMongoEntityList)) {
			Map<String, Map<String, Object>> uniqueFieldDataMd5Maps = new HashMap<>();
			for(Map<String, Object> findDmpInputMongoEntity : dbConvertInputMongoEntityList) {
				uniqueFieldDataMd5Maps.put(findDmpInputMongoEntity.get(MONGO_BASE_UNIQUEENCRYPT).toString(), findDmpInputMongoEntity);
			}
			
			for(Map.Entry<String, Map<String, Object>> md5DmpInputMongoEntityMap : md5DmpInputMongoEntityMaps.entrySet()) {
				Map<String, Object> findEntity = uniqueFieldDataMd5Maps.get(md5DmpInputMongoEntityMap.getKey());
				Map<String, Object> waitEntity = md5DmpInputMongoEntityMap.getValue();
				
				if(findEntity != null) {
					waitEntity.put(MONGO_BASE_ID, findEntity.get(MONGO_BASE_ID).toString());
					waitEntity.put(MONGO_BASE_MONGOCREATETIME, findEntity.get(MONGO_BASE_MONGOCREATETIME).toString());
					updateDmpInputMongoEntityList.add(waitEntity);
					if(!findEntity.get(MONGO_BASE_DATAENCRYPT).toString().equals(waitEntity.get(MONGO_BASE_DATAENCRYPT).toString())) {
						updateDmpInputMongoEntityList.add(waitEntity);
					}
				}else {
					saveDmpInputMongoEntityList.add(waitEntity);
				}
			}
		}else {
			saveDmpInputMongoEntityList.addAll(md5DmpInputMongoEntityMaps.values());
		}
		changeConvertInputMongoEntityList.addAll(saveDmpInputMongoEntityList);
	}
	
	public abstract List<Map<String, Object>> parseFdsToMongo(DmpInputMongoRequest dmpRequest, DmpInputFdsResponse dmpResponse);
	public abstract List<Map<String, Object>> parseInitToMongo(DmpInputMongoRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract List<Map<String, Object>> parseNoneToMongo(DmpInputMongoRequest dmpRequest, DmpInputTaskResponse dmpResponse);
	
	@Override
	protected List<String> getNextLevelIdList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		DmpInputMongoResponse dmpInputMongoResponse = (DmpInputMongoResponse) dmpResponse;
		List<Map<String, Object>> list = dmpInputMongoResponse.getConvertInputMongoEntityListMaps().get(dmpCfgInputConvertEntity);
		if(CollUtil.isNotEmpty(list)) {
			return list.stream().map(m -> m.get(MONGO_BASE_ID).toString()).collect(Collectors.toList());
		}else {
			return super.getNextLevelIdList(dmpRequest, dmpResponse);
		}
	}
}
