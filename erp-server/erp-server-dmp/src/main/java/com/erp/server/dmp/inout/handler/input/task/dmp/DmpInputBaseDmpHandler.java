package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputMongoDmpRelationEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpInputMongoDmpRelationService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.MD5;

@Service
@Scope("prototype")
public class DmpInputBaseDmpHandler extends DmpInputDmpHandler{

	@Autowired
	private DmpInputMongoDmpRelationService dmpInputMongoDmpRelationService;
	
	protected final MD5 md5 = MD5.create();
	
	@Override
	public List<BaseEntity> convertMongoToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputMongoResponse dmpResponse) {
		List<DmpInputMongoDmpRelationEntity> dmpInputDataDmpRelationEntityList = new ArrayList<>();
		DmpInputMongoDmpRelationEntity dmpInputMongoDmpRelationEntity = null;
		
		Map<String , Map<String, Object>> beanDmpInputDmpEntityMaps = new HashMap<>();
		
		Map<DmpCfgInputConvertEntity, List<Map>> convertInputMongoEntityListMaps = dmpResponse.getConvertInputMongoEntityListMaps();
		for(Map.Entry<DmpCfgInputConvertEntity, List<Map>> convertInputMongoEntityListMap : convertInputMongoEntityListMaps.entrySet()) {
			List<Map> dmpInputMongoEntityList = convertInputMongoEntityListMap.getValue();
			if(CollUtil.isNotEmpty(dmpInputMongoEntityList)) {
				Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = this.convertData(dmpInputMongoEntityList);
				for (Map.Entry<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
					List<TreeMap<String , Object>> dmpInputDmpBaseEntityList = dmpInputDataDmpRelationMap.getValue();
					
					for(TreeMap<String , Object> dmpInputDmpBaseEntity : dmpInputDmpBaseEntityList) {
						StringBuilder uniqueFieldMd5Sb = new StringBuilder();
						uniqueFieldMd5Sb.append(nextLevelId);
						StringBuilder dataMd5Sb = new StringBuilder();
						dataMd5Sb.append(nextLevelId);
						
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
							dmpInputDataDmpRelationEntityList.add(dmpInputMongoDmpRelationEntity);
						}
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
						updateDmpInputDmpEntityList.add(JSON.parseObject(JSON.toJSONString(waitEntity), dmpEntityClass));
					}else {
						saveDmpInputDmpEntityList.add(JSON.parseObject(JSON.toJSONString(waitEntity), dmpEntityClass));
					}
				}
			}else {
				saveDmpInputDmpEntityList = (List<BaseEntity>) JSON.parseArray(JSON.toJSONString(beanDmpInputDmpEntityMaps.values()) , dmpEntityClass);
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
	                .update();
			}
		}
		
		if(CollUtil.isNotEmpty(dmpInputDataDmpRelationEntityList)) {
			dmpInputMongoDmpRelationService.saveBatch(dmpInputDataDmpRelationEntityList);
		}
		
		return saveDmpInputDmpEntityList;
	}
	
	protected Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> convertData(List<Map> dmpInputMongoEntityList) {
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
	
	/**
	 * 转换key
	 * @param originalKey
	 * @return
	 */
	protected String convertKey(String originalKey) {
		return originalKey;
	}
	
	private void afterDmpInputDmpEntity(Map<String, Object> beanDmpInputDmpEntity) {
		beanDmpInputDmpEntity.put(StrUtils.underlineToCamel(INPUT_TASK_ID, true), inputTaskId);
		beanDmpInputDmpEntity.put(StrUtils.underlineToCamel(CONVERT_ID, true), convertId);
		beanDmpInputDmpEntity.put(StrUtils.underlineToCamel(NEXT_LEVEL_ID, true), nextLevelId);
	}

	@Override
	public List<BaseEntity> convertFdsToDmp(DmpInputDmpRequest dmpRequest, DmpInputFdsResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BaseEntity> convertInitToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputInitResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BaseEntity> convertNoneToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}
}
