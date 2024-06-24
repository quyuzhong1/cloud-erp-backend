package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputMongoDmpRelationEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpInputMongoDmpRelationService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.crypto.digest.MD5;

@Service
public abstract class DmpInputDmpHandler extends DmpInputTaskHandler{
	
	protected Set<String> uniqueFieldSet = new HashSet<>();
	protected boolean allFieldFlag;
	protected String storageName;
	protected String entityClassName;
	protected Class<? extends BaseEntity> dmpEntityClass;
	protected Set<String> entityFieldNameSet;
	protected ServiceImpl dmpEntityServiceImpl;
	protected final MD5 md5 = MD5.create();
	
	public static final String INPUT_TASK_ID = "input_task_id";
	public static final String CONVERT_ID = "convert_id";
	protected static final String NEXT_LEVEL_ID = "next_level_id";
	protected static final String UNIQUE_ENCRYPT = "unique_encrypt";
	protected static final String DATA_ENCRYPT = "data_encrypt";
	protected static final String MAIN_ID = "main_id";
	
	@Autowired
	private DmpInputMongoDmpRelationService dmpInputMongoDmpRelationService;
	
	@Override
	public void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputDmpRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpInputDmpResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpInputDmpRequest) dmpRequest, (DmpInputDmpResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpInputDmpRequest dmpRequest, DmpInputDmpResponse dmpResponse, DmpHandlerChain chain) {
		allFieldFlag = DmpHandlerUtils.getAllFieldFlag(dmpCfgInputConvertEntity.getUniqueFieldName(), uniqueFieldSet);
		storageName = dmpCfgInputConvertEntity.getStorageName();
		entityClassName = "com.erp.model.dmp.entity." + StrUtils.underlineToCamel(storageName, false) + "Entity";
		try {
			dmpEntityClass = (Class<? extends BaseEntity>) Class.forName(entityClassName);
		} catch (ClassNotFoundException e) {
			throw new ServiceException(entityClassName + "类不存在，异常信息：" + ExceptionUtil.stacktraceToString(e));
		}
		entityFieldNameSet = Stream.of(dmpEntityClass.getDeclaredFields()).filter(f -> f.getAnnotation(TableField.class) != null).map(Field::getName).collect(Collectors.toSet());
		dmpEntityServiceImpl = this.getServiceImpl(storageName);
		updateTaskStatus = DmpInputTaskStatusEnum.DMP;
		this.beforeToDoStatus(dmpRequest, dmpResponse);
		
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		wrapper.eq(CONVERT_ID, convertId);
		List<BaseEntity> dmpInputDmpBaseEntityList = dmpEntityServiceImpl.list(wrapper);
		
		if(CollUtil.isNotEmpty(dmpInputDmpBaseEntityList)) {
			this.isNextStatus(dmpResponse);
		}else {
			List<Map<String, Object>> inputMongoEntityList = null;
			this.dealDmpInputMongoBaseEntityList(dmpResponse);
			Map<DmpCfgInputConvertEntity, List<Map<String, Object>>> convertInputMongoEntityListMaps = dmpResponse.getConvertInputMongoEntityListMaps();
			if(convertInputMongoEntityListMaps != null && convertInputMongoEntityListMaps.size() > 0) {
				inputMongoEntityList = this.convertMongoToDmp(dmpRequest, dmpResponse);
			}else {
				this.dealConvertInputTaskFileEntityListMaps(dmpResponse);
				Map<DmpCfgInputConvertEntity, List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = dmpResponse.getConvertInputTaskFileEntityListMaps();
				if(convertInputTaskFileEntityListMaps != null && convertInputTaskFileEntityListMaps.size() > 0) {
					inputMongoEntityList = this.convertFdsToDmp(dmpRequest, dmpResponse);
				}else {
					Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = dmpResponse.getConvertInputTaskInitDTOListMaps();
					if(convertInputTaskInitDTOListMaps != null && convertInputTaskInitDTOListMaps.size() > 0) {
						inputMongoEntityList = this.convertInitToDmp(dmpRequest, dmpResponse);
					}else {
						inputMongoEntityList = this.convertNoneToDmp(dmpRequest, dmpResponse);
					}
				}
			}
			this.convertToDmp(inputMongoEntityList);
		}
		
		dmpResponse.getConvertInputDmpBaseEntityListMaps().put(dmpCfgInputConvertEntity, dmpInputDmpBaseEntityList);
		this.afterToDoStatus(dmpRequest, dmpResponse);
		
		DmpOutputDmpRequest dmpOutputDmpRequest = new DmpOutputDmpRequest();
		dmpOutputDmpRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
		dmpOutputDmpRequest.setConvertInputTaskFileEntityListMaps(dmpResponse.getConvertInputTaskFileEntityListMaps());
		dmpOutputDmpRequest.setConvertInputMongoEntityListMaps(dmpResponse.getConvertInputMongoEntityListMaps());
		dmpOutputDmpRequest.setConvertInputDmpBaseEntityListMaps(dmpResponse.getConvertInputDmpBaseEntityListMaps());
		this.doBaseChain(dmpRequest, dmpResponse, chain, dmpOutputDmpRequest);
	}
	
	protected List<BaseEntity> convertToDmp(List<Map<String, Object>> inputMongoEntityList){
		List<DmpInputMongoDmpRelationEntity> dmpInputDataDmpRelationEntityList = new ArrayList<>();
		DmpInputMongoDmpRelationEntity dmpInputMongoDmpRelationEntity = null;
		
		Map<String , Map<String, Object>> beanDmpInputDmpEntityMaps = new HashMap<>();
		if(CollUtil.isNotEmpty(inputMongoEntityList)) {
			Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = this.convertData(inputMongoEntityList);
			for (Map.Entry<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
				List<TreeMap<String , Object>> dmpInputDmpBaseEntityList = dmpInputDataDmpRelationMap.getValue();
				
				for(TreeMap<String , Object> dmpInputDmpBaseEntity : dmpInputDmpBaseEntityList) {
					StringBuilder uniqueFieldMd5Sb = new StringBuilder();
					StringBuilder dataMd5Sb = new StringBuilder();
					
					Map<String, Object> beanDmpInputDmpEntity = new HashMap<>();
					for (Map.Entry<String , Object> entry : dmpInputDmpBaseEntity.entrySet()) {
						String fieldName = entry.getKey().toString();
						if(!DmpInputMongoHandler.mongoBaseFiledList.contains(fieldName) && entityFieldNameSet.contains(fieldName)) {
							String key = entry.getKey().toString();
							Object value = entry.getValue();
							if(value != null) {
								if(allFieldFlag || uniqueFieldSet.contains(key)) {
									uniqueFieldMd5Sb.append(value);
								}
								dataMd5Sb.append(value);
							}
							beanDmpInputDmpEntity.put(key, value);
						}
		            }
					
					this.afterDmpInputDmpEntity(beanDmpInputDmpEntity);
					
					String id = DmpHandlerUtils.getId();
					beanDmpInputDmpEntity.put(BaseEntity.ID, id);
					String digestHex = md5.digestHex(uniqueFieldMd5Sb.toString());
					beanDmpInputDmpEntity.put(UNIQUE_ENCRYPT, digestHex);
					beanDmpInputDmpEntity.put(DATA_ENCRYPT, md5.digestHex(dataMd5Sb.toString()));
					beanDmpInputDmpEntityMaps.put(digestHex, beanDmpInputDmpEntity);
					
					List<Map<String, Object>> newDmpInputMongoEntityList = dmpInputDataDmpRelationMap.getKey();
					for(Map<String, Object> newDmpInputMongoEntity : newDmpInputMongoEntityList) {
						dmpInputMongoDmpRelationEntity = new DmpInputMongoDmpRelationEntity();
						dmpInputMongoDmpRelationEntity.setMongoId(newDmpInputMongoEntity.get(DmpInputMongoHandler.MONGO_BASE_ID).toString());
						dmpInputMongoDmpRelationEntity.setDmpId(id);
						dmpInputMongoDmpRelationEntity.setConvertId(convertId);
						dmpInputDataDmpRelationEntityList.add(dmpInputMongoDmpRelationEntity);
					}
				}
				
			}
		}
	
		
		List<BaseEntity> saveDmpInputDmpEntityList = new ArrayList<>();
		if(beanDmpInputDmpEntityMaps.size() > 0) {
			List<BaseEntity> updateDmpInputDmpEntityList = new ArrayList<>();
			List<String> deleteDmpIdList = new ArrayList<>();
			
			QueryWrapper<?> wrapper = new QueryWrapper<>();
			wrapper.in(UNIQUE_ENCRYPT, beanDmpInputDmpEntityMaps.keySet());
			List<Map<String, Object>> listMaps = dmpEntityServiceImpl.listMaps(wrapper);
			if(CollUtil.isNotEmpty(listMaps)) {
				Map<String, Map<String, Object>> uniqueMaps = new HashMap<>();
				for(Map<String, Object> listMap : listMaps) {
					uniqueMaps.put(listMap.get(UNIQUE_ENCRYPT).toString(), listMap);
				}
				
				for(Map.Entry<String , Map<String, Object>> beanDmpInputDmpEntityMap : beanDmpInputDmpEntityMaps.entrySet()) {
					Map<String, Object> findEntity = uniqueMaps.get(beanDmpInputDmpEntityMap.getKey());
					Map<String, Object> waitEntity = beanDmpInputDmpEntityMap.getValue();
					if(findEntity != null) {
						String dmpId = findEntity.get(BaseEntity.ID).toString();
						String waitDmpId = waitEntity.get(BaseEntity.ID).toString();
						waitEntity.put(BaseEntity.ID, dmpId);
						dmpInputDataDmpRelationEntityList.forEach(d -> {
							if(waitDmpId.equals(d.getDmpId())) {
								d.setDmpId(dmpId);
							}
						});
						waitEntity.put(BaseEntity.CREATE_TIME, findEntity.get(BaseEntity.CREATE_TIME));
						deleteDmpIdList.add(dmpId);
						updateDmpInputDmpEntityList.add(BeanUtil.toBeanIgnoreError(waitEntity, dmpEntityClass));
					}else {
						saveDmpInputDmpEntityList.add(BeanUtil.toBeanIgnoreError(waitEntity, dmpEntityClass));
					}
				}
			}else {
				saveDmpInputDmpEntityList = beanDmpInputDmpEntityMaps.values().stream().map(waitEntity -> BeanUtil.toBeanIgnoreError(waitEntity, dmpEntityClass)).collect(Collectors.toList());
			}
			
			if(CollUtil.isNotEmpty(saveDmpInputDmpEntityList)) {
				dmpEntityServiceImpl.saveBatch(saveDmpInputDmpEntityList);
			}
			if(CollUtil.isNotEmpty(updateDmpInputDmpEntityList)) {
				dmpEntityServiceImpl.updateBatchById(updateDmpInputDmpEntityList);
				saveDmpInputDmpEntityList.addAll(updateDmpInputDmpEntityList);
			}
			if(CollUtil.isNotEmpty(deleteDmpIdList)) {
				dmpInputMongoDmpRelationService.lambdaUpdate()
					.set(DmpInputMongoDmpRelationEntity::getIsDeleted, true)
	                .set(DmpInputMongoDmpRelationEntity::getUpdateTime, LocalDateTime.now())
	                .in(DmpInputMongoDmpRelationEntity::getDmpId, deleteDmpIdList)
	                .eq(DmpInputMongoDmpRelationEntity::getIsDeleted, false)
	                .eq(DmpInputMongoDmpRelationEntity::getConvertId, convertId)
	                .update();
			}
		}
		
		if(CollUtil.isNotEmpty(dmpInputDataDmpRelationEntityList)) {
			dmpInputMongoDmpRelationService.saveBatch(dmpInputDataDmpRelationEntityList);
		}
		
		return saveDmpInputDmpEntityList;
	}
	
	protected Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> convertData(List<Map<String, Object>> dmpInputMongoEntityList) {
		Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();
		for(Map dmpInputMongoBaseEntity : dmpInputMongoEntityList) {
			TreeMap dmpInputDmpBaseEntity = new TreeMap<>();
			Set<Entry> entrySet = dmpInputMongoBaseEntity.entrySet();
			for (Map.Entry entry : entrySet) {
				dmpInputDmpBaseEntity.put(this.convertKey(entry.getKey().toString()), entry.getValue());
			}
			dmpInputDataDmpRelationMaps.put(Collections.singletonList(dmpInputMongoBaseEntity), Collections.singletonList(dmpInputDmpBaseEntity));
		}
		return dmpInputDataDmpRelationMaps;
	}
	
	protected void afterDmpInputDmpEntity(Map<String, Object> beanDmpInputDmpEntity) {
		beanDmpInputDmpEntity.put(StrUtils.underlineToCamel(CONVERT_ID, true), convertId);
		this.afterDmpInputMongoEntityFixedValue(beanDmpInputDmpEntity);
	}
	
	protected void afterDmpInputMongoEntityFixedValue(Map<String , Object> beanDmpInputDmpEntity) {
		String fixedValueJson = dmpCfgInputConvertEntity.getFixedValueJson();
		if(StringUtils.isNotBlank(fixedValueJson)) {
			JSONObject fixedValueStr = JSON.parseObject(fixedValueJson);
			for(Map.Entry<String, Object> fixedValue : fixedValueStr.entrySet()) {
				beanDmpInputDmpEntity.put(fixedValue.getKey(), fixedValue.getValue());
			}
		}
	}
	
	protected String getMainConvertId() {
		List<DmpCfgInputConvertEntity> list = dmpCfgInputConvertService.lambdaQuery()
				.eq(DmpCfgInputConvertEntity::getMainId, dmpCfgInputConvertEntity.getMainId())
				.eq(DmpCfgInputConvertEntity::getInputStatus, dmpCfgInputConvertEntity.getInputStatus())
				.orderByAsc(DmpCfgInputConvertEntity::getOrder).list();
		return list.get(0).getId();
	}
	
	public abstract List<Map<String, Object>> convertMongoToDmp(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse);
	
	public abstract List<Map<String, Object>> convertFdsToDmp(DmpInputDmpRequest dmpRequest, DmpInputFdsResponse dmpResponse);
	public abstract List<Map<String, Object>> convertInitToDmp(DmpInputDmpRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract List<Map<String, Object>> convertNoneToDmp(DmpInputDmpRequest dmpRequest, DmpInputTaskResponse dmpResponse);
	
	@Override
	protected List<String> getNextLevelIdList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		DmpInputDmpResponse dmpInputDmpResponse = (DmpInputDmpResponse) dmpResponse;
		List<BaseEntity> list = dmpInputDmpResponse.getConvertInputDmpBaseEntityListMaps().get(dmpCfgInputConvertEntity);
		if(CollUtil.isNotEmpty(list)) {
			return list.stream().map(BaseEntity::getId).collect(Collectors.toList());
		}else {
			return super.getNextLevelIdList(dmpRequest, dmpResponse);
		}
	}
}
