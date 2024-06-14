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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputDmpBaseEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.IdSequenceUtils;
import com.erp.server.dmp.service.DmpInputTaskService;

@Service
@Scope("prototype")
public class DmpInputBaseDmpHandler extends DmpInputDmpHandler{

	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	
	@Override
	public List<DmpInputDmpBaseEntity> convertMongoToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputMongoResponse dmpResponse) {
		String inputTaskId = dmpRequest.getInputTaskId();
		List<DmpInputDmpBaseEntity> dmpInputDmpBaseEntityList = new ArrayList<>();
		
		List<Map> dmpInputMongoEntityList = dmpResponse.getDmpInputMongoEntityList();
		for(Map dmpInputMongoBaseEntity : dmpInputMongoEntityList) {
			BeanGenerator beanGenerator = new BeanGenerator();
			beanGenerator.setSuperclass(DmpInputDmpBaseEntity.class);
			Object dmpInputDmpEntity = beanGenerator.create();
			
			BeanMap beanMap = BeanMap.create(dmpInputDmpEntity);
			
			Set<Entry> entrySet = dmpInputMongoBaseEntity.entrySet();
			for (Map.Entry entry : entrySet) {
                String fieldName = entry.getKey().toString();
				beanGenerator.addProperty(fieldName, Object.class);
                
                beanMap.put(fieldName, entry.getValue());
            }
			
			beanGenerator.addProperty("id", String.class);
			beanMap.put("id", IdSequenceUtils.getId());
			
			dmpInputDmpBaseEntityList.add((DmpInputDmpBaseEntity)dmpInputDmpEntity);
		}
		
		ServiceImpl bean = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(dmpCfgInputConvertEntity.getStorageName(), true) + "ServiceImpl" , ServiceImpl.class);
		
		bean.saveBatch(dmpInputDmpBaseEntityList);
		
		dmpInputTaskService.lambdaUpdate()
				.eq(DmpInputTaskEntity::getId, inputTaskId)
				.set(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.DMP.getCode())
				.update();
		
		return dmpInputDmpBaseEntityList;
	}

	@Override
	public List<DmpInputDmpBaseEntity> convertFdsToDmp(DmpInputDmpRequest dmpRequest, DmpInputFdsResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DmpInputDmpBaseEntity> convertInitToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputInitResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DmpInputDmpBaseEntity> convertNoneToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
