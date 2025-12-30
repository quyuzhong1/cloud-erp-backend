package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;

import com.erp.model.dmp.dto.DmpCfgInputConvertValueDTO;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputMongoDmpRelationEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpInputMongoDmpRelationService;
import com.google.common.collect.Lists;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.extra.spring.SpringUtil;

/**
 * dmp输入任务dmp状态处理器，被dmp任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
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
	protected List<BaseEntity> changeConvertInputDmpBaseEntityList = new ArrayList<>();
	protected Set<String> deleteConvertInputDmpBaseEntitySet = new HashSet<>();
	
	public static final String INPUT_TASK_ID = "input_task_id";
	public static final String CONVERT_ID = "convert_id";
	protected static final String NEXT_LEVEL_ID = "next_level_id";
	protected static final String UNIQUE_ENCRYPT = "unique_encrypt";
	protected static final String DATA_ENCRYPT = "data_encrypt";
	protected static final String MAIN_ID = "main_id";
	
	@Autowired
	private DmpInputMongoDmpRelationService dmpInputMongoDmpRelationService;
	
	@Resource
    private MQProducerService<String> mqProducerService;
	private static Set<String> SKU_LISTING_TIME_SET = new HashSet<>();
	private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");
	@Resource
	protected RedisTemplate<String,Object> redisTemplate;
	@Autowired
	@Qualifier("dmpListTimeExecutorPool")
	protected ExecutorService dmpListTimeExecutorPool;
	
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
			dmpInputDmpBaseEntityList = this.convertToDmp(inputMongoEntityList);
		}
		
		dmpResponse.getConvertInputDmpBaseEntityListMaps().put(dmpCfgInputConvertEntity, dmpInputDmpBaseEntityList);
		if(storageName.equals("dmp_so_detail") && CollUtil.isNotEmpty(dmpInputDmpBaseEntityList)) {
			dmpListTimeExecutorPool.execute(() -> pushSoInfoSkuListingTime(dmpResponse.getConvertInputDmpBaseEntityListMaps()));
		}
		dmpResponse.getChangeConvertInputDmpBaseEntityListMaps().put(dmpCfgInputConvertEntity, changeConvertInputDmpBaseEntityList);
		dmpResponse.getDeleteConvertInputDmpBaseEntityMaps().put(dmpCfgInputConvertEntity, deleteConvertInputDmpBaseEntitySet);
		this.afterToDoStatus(dmpRequest, dmpResponse);
		
		DmpOutputTaskRequest dmpOutputDmpRequest = new DmpOutputTaskRequest();
		dmpOutputDmpRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
		dmpOutputDmpRequest.setConvertInputTaskFileEntityListMaps(dmpResponse.getConvertInputTaskFileEntityListMaps());
		dmpOutputDmpRequest.setConvertInputMongoEntityListMaps(dmpResponse.getConvertInputMongoEntityListMaps());
		dmpOutputDmpRequest.setConvertInputDmpBaseEntityListMaps(dmpResponse.getConvertInputDmpBaseEntityListMaps());
		dmpOutputDmpRequest.setChangeConvertInputDmpBaseEntityListMaps(dmpResponse.getChangeConvertInputDmpBaseEntityListMaps());
		dmpOutputDmpRequest.setDeleteConvertInputDmpBaseEntityMaps(dmpResponse.getDeleteConvertInputDmpBaseEntityMaps());
		this.doBaseChain(dmpRequest, dmpResponse, chain, dmpOutputDmpRequest);
	}

	protected void afterDmpInputConvertValue(Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps) {
		Map<String, List<String>> originaConvertMap = new HashMap<>();

		List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> dmpCfgInputConvertValue = dmpHandlerCache.getDmpCfgInputConvertValue(convertId);
		if (CollectionUtil.isEmpty(dmpCfgInputConvertValue)) {
			return;
		}
		for (Map.Entry<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpInputDmpBaseEntityList = dmpInputDataDmpRelationMap.getValue();
			for (TreeMap<String, Object> dmpInputDmpBaseEntity : dmpInputDmpBaseEntityList) {
				for (Map.Entry<String , Object> entry : dmpInputDmpBaseEntity.entrySet()) {
/*
					String originalKey = entry.getKey();
*/
/*					if (!".".contains(originalKey)) {
						continue;
					}*//*

					List<String> convertKey = originaConvertMap.get(originalKey);
					if(convertKey == null) {
						convertKey = this.convertKey(originalKey);
						originaConvertMap.put(originalKey, convertKey);
					}
					for(String c : convertKey) {
						dmpInputDmpBaseEntity.put(c, DmpHandlerUtils.getValueByPath(entry.getValue(), entry.getKey()));
					}

					dmpCfgInputConvertValue.stream().filter()
					DmpCfgInputConvertValueDTO.MappingAndValueDTO mappingAndValueDTO = dmpCfgInputConvertValue.stream().filter(req -> StrUtils.underlineToCamel(req.getConvertKey(), true).equals(entry.getKey())).findFirst().orElse(null);

					if (ObjectUtil.isNotEmpty(mappingAndValueDTO)) {
						if (".".contains(mappingAndValueDTO.getOriginalKey())) {
//							DmpHandlerUtils.getValueByPath(entry.getValue(), mappingAndValueDTO.getOriginalKey(), mappingAndValueDTO.getConvertKey());
						}
					}
*/
					String mappingAndValue = dmpCfgInputConvertValue.stream()
							.filter(req -> StringUtils.isNotBlank(req.getConvertBeforeValue())
									&& StrUtils.underlineToCamel(req.getConvertKey(), true).equals(entry.getKey())
									&& req.getConvertBeforeValue().equals(entry.getValue()))
							.map(req -> StringUtils.isNotBlank(req.getConvertAfterValue()) ? req.getConvertAfterValue() : "")
							.findFirst().orElse("");
					if (StringUtils.isNotBlank(mappingAndValue)) {

						dmpInputDmpBaseEntity.put(entry.getKey(), mappingAndValue);
					}
				}
			}
		}
	}

	protected List<BaseEntity> convertToDmp(List<Map<String, Object>> inputMongoEntityList){
		List<DmpInputMongoDmpRelationEntity> dmpInputDataDmpRelationEntityList = new ArrayList<>();
		DmpInputMongoDmpRelationEntity dmpInputMongoDmpRelationEntity = null;
		
		Map<String , Map<String, Object>> beanDmpInputDmpEntityMaps = new HashMap<>();
		if(CollUtil.isNotEmpty(inputMongoEntityList)) {
			Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = this.convertData(inputMongoEntityList);

			//值映射
			afterDmpInputConvertValue(dmpInputDataDmpRelationMaps);

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
					beanDmpInputDmpEntity.put(BaseEntity.FIELD_ID, id);
					String digestHex = md5.digestHex(uniqueFieldMd5Sb.toString());
					beanDmpInputDmpEntity.put(UNIQUE_ENCRYPT, digestHex);
					beanDmpInputDmpEntity.put(DATA_ENCRYPT, md5.digestHex(dataMd5Sb.toString()));
					Map<String, Object> map = beanDmpInputDmpEntityMaps.get(digestHex);
					if(map != null) {
						dmpInputDataDmpRelationEntityList.removeIf(d -> map.get(BaseEntity.FIELD_ID).equals(d.getDmpId()));
					}
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
			
			List<Map<String, Object>> listMaps = new ArrayList<>();
			List<String> uniqueEncryptList = new ArrayList<>(beanDmpInputDmpEntityMaps.keySet());
			List<List<String>> partition = Lists.partition(uniqueEncryptList, 50000);
			for(List<String> p : partition) {
				QueryWrapper<?> wrapper = new QueryWrapper<>();
				wrapper.in(UNIQUE_ENCRYPT, p);
				listMaps.addAll(dmpEntityServiceImpl.listMaps(wrapper));
			}
			if(CollUtil.isNotEmpty(listMaps)) {
				Map<String, Map<String, Object>> uniqueMaps = new HashMap<>();
				for(Map<String, Object> listMap : listMaps) {
					uniqueMaps.put(listMap.get(UNIQUE_ENCRYPT).toString(), listMap);
				}
				
				for(Map.Entry<String , Map<String, Object>> beanDmpInputDmpEntityMap : beanDmpInputDmpEntityMaps.entrySet()) {
					String key = beanDmpInputDmpEntityMap.getKey();
					Map<String, Object> findEntity = uniqueMaps.get(key);
					Map<String, Object> waitEntity = beanDmpInputDmpEntityMap.getValue();
					if(findEntity != null) {
						String dmpId = findEntity.get(BaseEntity.FIELD_ID).toString();
						String waitDmpId = waitEntity.get(BaseEntity.FIELD_ID).toString();
						waitEntity.put(BaseEntity.FIELD_ID, dmpId);
						dmpInputDataDmpRelationEntityList.forEach(d -> {
							if(waitDmpId.equals(d.getDmpId())) {
								d.setDmpId(dmpId);
							}
						});
						waitEntity.put(BaseEntity.CREATE_TIME, findEntity.get(BaseEntity.CREATE_TIME));
						deleteDmpIdList.add(dmpId);
						BaseEntity waitBeanEntity = DmpHandlerUtils.toBeanIgnoreError(waitEntity, dmpEntityClass);
						updateDmpInputDmpEntityList.add(waitBeanEntity);
						if(!findEntity.get(DATA_ENCRYPT).toString().equals(waitEntity.get(DATA_ENCRYPT).toString())) {
							changeConvertInputDmpBaseEntityList.add(waitBeanEntity);
						}
					}else {
						saveDmpInputDmpEntityList.add(DmpHandlerUtils.toBeanIgnoreError(waitEntity, dmpEntityClass));
					}
				}
			}else {
				saveDmpInputDmpEntityList = beanDmpInputDmpEntityMaps.values().stream().map(waitEntity -> DmpHandlerUtils.toBeanIgnoreError(waitEntity, dmpEntityClass)).collect(Collectors.toList());
			}
			changeConvertInputDmpBaseEntityList.addAll(saveDmpInputDmpEntityList);
			
			if(CollUtil.isNotEmpty(saveDmpInputDmpEntityList)) {
				dmpEntityServiceImpl.saveBatch(saveDmpInputDmpEntityList);
			}
			if(CollUtil.isNotEmpty(updateDmpInputDmpEntityList)) {
				dmpEntityServiceImpl.updateBatchById(updateDmpInputDmpEntityList);
				saveDmpInputDmpEntityList.addAll(updateDmpInputDmpEntityList);
			}
			if(CollUtil.isNotEmpty(deleteDmpIdList)) {
				List<List<String>> deleteDmpIdPartition = Lists.partition(deleteDmpIdList, 50000);
				for(List<String> p : deleteDmpIdPartition) {
					dmpInputMongoDmpRelationService.lambdaUpdate()
						.set(DmpInputMongoDmpRelationEntity::getIsDeleted, true)
		                .set(DmpInputMongoDmpRelationEntity::getUpdateTime, LocalDateTime.now())
		                .in(DmpInputMongoDmpRelationEntity::getDmpId, p)
		                .eq(DmpInputMongoDmpRelationEntity::getIsDeleted, false)
		                .eq(DmpInputMongoDmpRelationEntity::getConvertId, convertId)
		                .update();
				}
			}
			if(needDealDetailDelete()) {
				this.dealDetailDelete(beanDmpInputDmpEntityMaps);
			}
		}
		
		if(CollUtil.isNotEmpty(dmpInputDataDmpRelationEntityList)) {
			dmpInputMongoDmpRelationService.saveBatch(dmpInputDataDmpRelationEntityList);
		}
		
		return saveDmpInputDmpEntityList;
	}
	
	/**
	 * 是否要处理明细删除
	 * @return
	 */
	protected boolean needDealDetailDelete(){
		return false;
	}
	
	/**
	 * 处理明细是否有删除
	 * @param beanDmpInputDmpEntityMaps
	 */
	protected void dealDetailDelete(Map<String , Map<String, Object>> beanDmpInputDmpEntityMaps){
		if(CollUtil.isNotEmpty(beanDmpInputDmpEntityMaps)) {
			Collection<Map<String, Object>> values = beanDmpInputDmpEntityMaps.values();
			List<Object> list = new ArrayList<>();
			String mainId = StrUtils.underlineToCamel(MAIN_ID, true);
			for(Map<String, Object> value : values) {
				Object v = value.get(mainId);
				if(v != null && StringUtils.isNotBlank(v.toString())) {
					list.add(v);
				}
			}
			QueryWrapper<?> wrapper = new QueryWrapper<>();
			wrapper.in(MAIN_ID, list);
			List<Map<String, Object>> dbListMaps = dmpEntityServiceImpl.listMaps(wrapper);
			if(CollUtil.isNotEmpty(dbListMaps)) {
				for(Map<String, Object> dbListMap : dbListMaps) {
					if(!beanDmpInputDmpEntityMaps.containsKey(dbListMap.get(UNIQUE_ENCRYPT).toString())) {
						String deleteId = dbListMap.get(BaseEntity.FIELD_ID).toString();
						deleteConvertInputDmpBaseEntitySet.add(deleteId);
					}
				}
				if(CollUtil.isNotEmpty(deleteConvertInputDmpBaseEntitySet)) {
					dmpEntityServiceImpl.removeByIds(deleteConvertInputDmpBaseEntitySet);
				}
			}
		}
	}
	
	protected Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> convertData(List<Map<String, Object>> dmpInputMongoEntityList) {
		this.beforeConvertData(dmpInputMongoEntityList);
		Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();
		Map<String, List<String>> originaConvertMap = new HashMap<>();
		for(Map<String, Object> dmpInputMongoBaseEntity : dmpInputMongoEntityList) {
			TreeMap<String , Object> dmpInputDmpBaseEntity = new TreeMap<>();
			Set<Entry<String, Object>> entrySet = dmpInputMongoBaseEntity.entrySet();
			for (Map.Entry<String, Object> entry : entrySet) {
				String originalKey = entry.getKey().toString();
				List<String> convertKey = originaConvertMap.get(originalKey);
				if(convertKey == null) {
					convertKey = this.convertKey(originalKey);
					originaConvertMap.put(originalKey, convertKey);
				}
				for(String c : convertKey) {
					dmpInputDmpBaseEntity.put(c, entry.getValue());
				}
			}
			ArrayList<Map<String, Object>> keyList = new ArrayList<>();
			keyList.add(dmpInputMongoBaseEntity);
			ArrayList<TreeMap<String, Object>> valueList = new ArrayList<>();
			valueList.add(dmpInputDmpBaseEntity);
			this.afterDmpInputMongoEntityFixedValue(dmpInputDmpBaseEntity);
			dmpInputDataDmpRelationMaps.put(keyList, valueList);
		}
		this.afterConvertData(dmpInputDataDmpRelationMaps);
		return dmpInputDataDmpRelationMaps;
	}
	
	protected void beforeConvertData(List<Map<String, Object>> dmpInputMongoEntityList) {
		
	}
	
	protected void afterConvertData(Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps) {
		
	}
	
	protected void afterDmpInputDmpEntity(Map<String, Object> beanDmpInputDmpEntity) {
		beanDmpInputDmpEntity.put(StrUtils.underlineToCamel(CONVERT_ID, true), convertId);
		beanDmpInputDmpEntity.put(StrUtils.underlineToCamel(INPUT_TASK_ID, true), inputTaskId);
	}
	
	protected void afterDmpInputMongoEntityFixedValue(Map<String , Object> beanDmpInputDmpEntity) {
		String fixedValueJson = dmpCfgInputConvertEntity.getFixedValueJson();
		if(StringUtils.isNotBlank(fixedValueJson)) {
			JSONObject fixedValueStr = JSON.parseObject(fixedValueJson);
			for(Map.Entry<String, Object> fixedValue : fixedValueStr.entrySet()) {
				String key = fixedValue.getKey();
				Object object = beanDmpInputDmpEntity.get(key);
				if(object == null) {
					beanDmpInputDmpEntity.put(key, fixedValue.getValue());
				}else {
					if(object instanceof String && StringUtils.isBlank(object.toString())) {
						beanDmpInputDmpEntity.put(key, fixedValue.getValue());
					}
				}
			}
		}
	}
	
	protected DmpCfgInputConvertEntity getMainConvertId() {
		List<DmpCfgInputConvertEntity> list = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(dmpCfgInputConvertEntity.getMainId())
				&& d.getInputStatus().equals(dmpCfgInputConvertEntity.getInputStatus()));
		list.sort((d1 , d2) -> d1.getOrder().compareTo(d2.getOrder()));
		return list.get(0);
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

	private void pushSoInfoSkuListingTime(Map<DmpCfgInputConvertEntity , List<BaseEntity>> convertInputDmpBaseEntityListMaps) {
		Map<String , DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
		Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String currStorageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_so_info".equals(currStorageName)) {
					for(BaseEntity v : value) {
						DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
						dmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
					}
				}else if("dmp_so_detail".equals(currStorageName)) {
					for(BaseEntity v : value) {
						DmpSoDetailEntity dmpSoDetailEntity = (DmpSoDetailEntity) v;
						String mainId = dmpSoDetailEntity.getMainId();
						List<DmpSoDetailEntity> list = dmpSoDetailEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoDetailEntity);
						dmpSoDetailEntityMap.put(mainId, list);
					}
				}
			}
		}
		
		String tag = RocketMqNewTag.DMP_PRODUCT_LISTING_TO_PLM_TAG.replace("${spring.cloud.nacos.discovery.namespace}", namespace);
		for(Map.Entry<String, List<DmpSoDetailEntity>> dmpSoDetailEntity : dmpSoDetailEntityMap.entrySet()) {
			DmpSoInfoEntity dmpSoInfoEntity = dmpSoInfoEntityMap.get(dmpSoDetailEntity.getKey());
			if(dmpSoInfoEntity != null) {
				String sourcePlatform = dmpSoInfoEntity.getSourcePlatform();
				if(StringUtils.isNotBlank(sourcePlatform)) {
					List<DmpSoDetailEntity> dmpSoDetailEntityList = dmpSoDetailEntity.getValue();
					for(DmpSoDetailEntity detailEntity : dmpSoDetailEntityList) {
						String skuNo = detailEntity.getPlatformSku();
						if(StringUtils.isNotBlank(skuNo)) {
							String key = RedisKeyConstant.PRODUCT_LISTING_TIME + sourcePlatform + ":" + skuNo;
							if(!SKU_LISTING_TIME_SET.contains(key)) {
								Boolean hasKey = redisTemplate.hasKey(key);
								if(hasKey) {
									SKU_LISTING_TIME_SET.add(key);
								}else {
									String now = DateUtil.now();
									Map<String, String> mqData = new HashMap<>();
									mqData.put("sourcePlatform", sourcePlatform);
									mqData.put("sourceSystem", dmpSoInfoEntity.getSourceSystem());
									mqData.put("skuNo", skuNo);
									mqData.put("listingTime", now);
									mqProducerService.syncClassMsg(RocketMqNewTopic.DMP_PRODUCT_LISTING_TO_PLM_TOPIC, tag
											, JSON.toJSONString(mqData), key);
								}
							}
						}
					}
				}
			}
		}
	}

}
