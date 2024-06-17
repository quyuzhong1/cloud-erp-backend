package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputDataDmpRelationEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpInputDataDmpRelationService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;

@Service
@Scope("prototype")
public class DmpInputBaseDmpHandler extends DmpInputDmpHandler{

	@Autowired
	private DmpInputDataDmpRelationService dmpInputDataDmpRelationService;
	
	@Override
	public List<BaseEntity> convertMongoToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputMongoResponse dmpResponse) {
		List<BaseEntity> dmpInputDmpBaseEntityList = new ArrayList<>();
		List<DmpInputDataDmpRelationEntity> dmpInputDataDmpRelationEntityList = new ArrayList<>();
		DmpInputDataDmpRelationEntity dmpInputDataDmpRelationEntity = null;
		
		Map<DmpCfgInputConvertEntity, List<Map>> convertInputMongoEntityListMaps = dmpResponse.getConvertInputMongoEntityListMaps();
		for(Map.Entry<DmpCfgInputConvertEntity, List<Map>> convertInputMongoEntityListMap : convertInputMongoEntityListMaps.entrySet()) {
			List<Map> dmpInputMongoEntityList = convertInputMongoEntityListMap.getValue();
			if(CollUtil.isNotEmpty(dmpInputMongoEntityList)) {
				for(Map dmpInputMongoBaseEntity : dmpInputMongoEntityList) {
					BeanGenerator beanGenerator = new BeanGenerator();
					beanGenerator.setSuperclass(BaseEntity.class);
					
					Set<Entry> entrySet = dmpInputMongoBaseEntity.entrySet();
					for (Map.Entry entry : entrySet) {
		                String fieldName = entry.getKey().toString();
		                if(!("_" + BaseEntity.ID).equals(fieldName)) {
		                	beanGenerator.addProperty(fieldName, Object.class);
		                }
		                
		            }
					
					Object beanDmpInputDmpEntity = beanGenerator.create();
					
					BeanMap beanMap = BeanMap.create(beanDmpInputDmpEntity);
					
					for (Map.Entry entry : entrySet) {
						String fieldName = entry.getKey().toString();
		                beanMap.put(fieldName, entry.getValue());
		            }
					
					String id = DmpHandlerUtils.getId();
					beanMap.put(StrUtils.underlineToCamel(CONVERT_ID, true), convertId);
					
					BaseEntity newBeanDmpInputDmpEntity = (BaseEntity)beanDmpInputDmpEntity;
					newBeanDmpInputDmpEntity.setId(id);
					dmpInputDmpBaseEntityList.add(newBeanDmpInputDmpEntity);
					
					dmpInputDataDmpRelationEntity = new DmpInputDataDmpRelationEntity();
					dmpInputDataDmpRelationEntity.setDataId(dmpInputMongoBaseEntity.get(BaseEntity.ID).toString());
					dmpInputDataDmpRelationEntity.setDmpId(id);
					dmpInputDataDmpRelationEntity.setDataType(DmpInputTaskStatusEnum.MONGO.getCode());
					
					dmpInputDataDmpRelationEntityList.add(dmpInputDataDmpRelationEntity);
				}
			}
		}
		
		List<?> copyToList = null;
		if(CollUtil.isNotEmpty(dmpInputDmpBaseEntityList)) {
			String storageName = dmpCfgInputConvertEntity.getStorageName();
			try {
				copyToList = JSON.parseArray(JSON.toJSONString(dmpInputDmpBaseEntityList),  Class.forName("com.erp.model.dmp.entity." + StrUtils.underlineToCamel(storageName, false) + "Entity"));
			} catch (ClassNotFoundException e) {
				throw new ServiceException(ExceptionUtil.stacktraceToString(e));
			}
			
			this.getServiceImpl().saveBatch(copyToList);
			
			dmpInputDataDmpRelationService.saveBatch(dmpInputDataDmpRelationEntityList);
		}
		
		return (List<BaseEntity>) copyToList;
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
