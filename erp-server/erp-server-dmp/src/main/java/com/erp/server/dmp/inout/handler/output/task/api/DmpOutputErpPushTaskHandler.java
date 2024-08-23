package com.erp.server.dmp.inout.handler.output.task.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.dmp.enums.DmpCfgOutputTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpBasicSystemService;
import com.erp.server.dmp.service.DmpCfgApiService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputErpPushTaskHandler extends DmpOutputTaskHandler{

	@Autowired
	private DmpHandlerCache dmpHandlerCache;
	@Autowired
	private DmpBasicSystemService dmpBasicSystemService;
	@Autowired
	private DmpCfgApiService dmpCfgApiService;
	@Autowired
	private IdentifierGenerator identifierGenerator;
	@Autowired
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	
	@Override
	public List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpResponse.getDmpCfgOutputEntity();
		if(!dmpCfgOutputEntity.getType().equals(DmpCfgOutputTypeEnum.API.getCode())) {
			throw new ServiceException("非api输出类型，请勿配置DmpOutputErpPushTaskHandler");
		}
		List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
		
		String cfgOutputId = dmpCfgOutputEntity.getId();
		String mainId = dmpResponse.getDmpCfgInputConvertEntity().getMainId();
		DmpCfgInputEntity dmpCfgInputEntity = dmpHandlerCache.getDmpCfgInputEntityList(d -> d.getId().equals(mainId)).get(0);
		String typeId = dmpCfgInputEntity.getTypeId();
		String apiType = dmpCfgApiService.getById(typeId).getApiType();
		
		String systemId = dmpCfgInputEntity.getSystemId();
		String code = dmpBasicSystemService.getById(systemId).getCode();
		
		List<DmpPushMsgEntity> dmpPushMsgEntityList = new ArrayList<>();
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				for(BaseEntity v : value) {
					DmpPushMsgEntity dmpPushMsgEntity = (DmpPushMsgEntity) v;
					if(!dmpPushMsgEntity.getTargetPlatform().equals(code)) {
						continue;
					}
					if(!dmpPushMsgEntity.getSourceType().equals(apiType)) {
						continue;
					}
					if(this.validateDataBlack(dmpPushMsgEntity, cfgOutputId)) {
						continue;
					}
					dmpPushMsgEntityList.add(dmpPushMsgEntity);
				}
			}
		}
		
		String outputTypeId = dmpCfgOutputEntity.getTypeId();
		DmpCfgApiEntity outputDmpCfgApiEntity = dmpCfgApiService.getById(outputTypeId);
		String outputClass = outputDmpCfgApiEntity.getApiClass();
		Object bean = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(outputClass));
		String outputMethod = outputDmpCfgApiEntity.getApiType();
		Method method = null;
		try {
			method = bean.getClass().getMethod(outputMethod, Object.class);
		} catch (Exception e) {
			String msg = "输出任务获取" + outputClass + "类的 "+ outputMethod +"方法失败";
			log.error(msg , e);
			throw new ServiceException(msg);
		}
		if(CollUtil.isNotEmpty(dmpPushMsgEntityList)) {
			dmpPushMsgEntityList.sort((d1 , d2) -> d1.getMessageUpdateTime().compareTo(d2.getMessageUpdateTime()));
			int i = 0;
			Integer pushRate = dmpCfgOutputEntity.getPushRate();
			if(pushRate == null) {
				pushRate = 3;
			}
			for(DmpPushMsgEntity dmpPushMsgEntity : dmpPushMsgEntityList) {
				String dataId = dmpPushMsgEntity.getId();
				
				String pushData = dmpPushMsgEntity.getPushData();
				try {
					method.invoke(bean, pushData);
				} catch (Exception e) {
					String msg = "执行输出任务获取" + outputClass + "类的 "+ outputMethod +"方法失败";
					log.error(msg , e);
					throw new ServiceException(msg);
				}
				DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
				String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
				dmpOutputTaskRecordEntity.setId(id);
				dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
				dmpOutputTaskRecordEntity.setDataId(dataId);
				dmpOutputTaskRecordEntity.setSourceCode(dmpPushMsgEntity.getSourceCode());
				dmpOutputTaskRecordEntity.setRequestData(pushData);
				dmpOutputTaskRecordEntity.setStatus(DmpOutputTaskRecordStatusEnum.FINISH.getCode());
				dmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
				
				if(pushRate > 0) {
    				i = i + 1;
        			if(i % pushRate == 0) {
        				try {
        					Thread.sleep(1000);
        				} catch (InterruptedException e) {}
        			}
    			}
			}
			dmpOutputTaskRecordService.saveBatch(dmpOutputTaskRecordEntityList);
		}
		
		return dmpOutputTaskRecordEntityList;
	}
	
	
	
}
