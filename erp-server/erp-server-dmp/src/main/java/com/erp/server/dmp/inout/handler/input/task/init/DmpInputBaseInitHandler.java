package com.erp.server.dmp.inout.handler.input.task.init;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpCfgInputChildEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpCfgInputTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputKingdeeApiInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputKingdeeApiInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpCfgApiService;
import com.erp.server.dmp.service.DmpCfgInputChildService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Scope("prototype")
public class DmpInputBaseInitHandler extends DmpInputInitHandler{

	@Autowired
	private DmpCfgApiService dmpCfgApiService;
	
	@Autowired
	private MongoService mongoService;
	
	@Autowired
	private DmpCfgInputChildService dmpCfgInputChildService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		String type = dmpCfgInputEntity.getType();
		String typeId = dmpCfgInputEntity.getTypeId();
		if(DmpCfgInputTypeEnum.API.getCode().equals(type)) {
			DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
			String apiClass = dmpCfgApiEntity.getApiClass();
			DmpInputApiInitHandler dmpInputApiInitHandler = null;
			DmpInputKingdeeApiInitRequest dmpInputApiInitRequest = null;
			
			LocalDateTime startTime = dmpInputTaskEntity.getStartTime();
			LocalDateTime endTime = dmpInputTaskEntity.getEndTime();
			
			String parentStorageName = "";
			
			if(DmpBasicSystemCodeEnum.KINGDEE.getCode().equals(dmpBasicSystemEntity.getCode())) {
				dmpInputApiInitHandler = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(apiClass), DmpInputKingdeeApiInitHandler.class);
				dmpInputApiInitRequest = new DmpInputKingdeeApiInitRequest();
				dmpInputApiInitRequest = (DmpInputKingdeeApiInitRequest) dmpInputApiInitRequest;
				dmpInputApiInitRequest.setFormId(dmpCfgApiEntity.getApiType());
				
				String extendJson = dmpCfgInputEntity.getExtendJson();
				if(StringUtils.isNotBlank(extendJson)) {
					DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
					JSONObject parseObject = JSON.parseObject(extendJson);
					if("order".equals(dmpCfgInputEntity.getCode())) {
						dmpInputApiInitRequest.setFilterStr(parseObject.getString("filterStr")
								.replace("{startTime}", sdf.format(startTime))
								.replace("{endTime}", sdf.format(endTime)));
					}else if("orderItem".equals(dmpCfgInputEntity.getCode())) {
						Map findMongoDataById = null;
						parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
						if(StringUtils.isNotBlank(parentStorageName)) {
							try {
								findMongoDataById = mongoService.findMongoDataById(nextLevelId, parentStorageName, Map.class);
							} catch (Exception e) {
								log.error("金蝶orderItem的{}查询mongo的{}错误" , nextLevelId , parentStorageName , e);
							}
						}
						if(findMongoDataById == null) {
							return null;
						}
						dmpInputApiInitRequest.setFilterStr(parseObject.getString("filterStr")
								.replace("{FBillNo}", findMongoDataById.get("FBillNo").toString())
								.replace("{FID}", findMongoDataById.get("FID").toString()));
					}
					
					dmpInputApiInitRequest.setFieldKeys(parseObject.getString("fieldKeys"));
				}
			}
			dmpInputApiInitRequest.setStartTime(startTime);
			dmpInputApiInitRequest.setEndTime(endTime);
			
			return dmpInputApiInitHandler.getApiData(dmpInputApiInitRequest);
		}else if(DmpCfgInputTypeEnum.DB.getCode().equals(type)) {
			
		}else if(DmpCfgInputTypeEnum.MQ.getCode().equals(type)) {
			
		}
		return null;
	}
	
	private String getParentStorageName(DmpInputTaskStatusEnum taskStatusEnum) {
		String cfgInputId = dmpCfgInputEntity.getId();
		String parentStorageName = "";
		List<DmpCfgInputChildEntity> dmpCfgInputChildEntityList = dmpCfgInputChildService.lambdaQuery()
				.eq(DmpCfgInputChildEntity::getChildId, cfgInputId)
				.eq(DmpCfgInputChildEntity::getInputStatus, taskStatusEnum.getCode())
				.list();
		if(CollUtil.isNotEmpty(dmpCfgInputChildEntityList)) {
			String parentId = dmpCfgInputChildEntityList.get(0).getParentId();
			List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(parentId) 
					&& d.getInputStatus().equals(taskStatusEnum.getCode()));
			if(DmpInputTaskStatusEnum.MONGO == taskStatusEnum) {
				parentStorageName = DmpHandlerUtils.getMongoStorageName(dmpBasicSystemEntity, dmpCfgInputService.getById(parentId), dmpCfgInputConvertEntityList.get(0));
			}else if(DmpInputTaskStatusEnum.DMP == taskStatusEnum) {
				parentStorageName = dmpCfgInputConvertEntityList.get(0).getStorageName();
			}
		}
		return parentStorageName;
	}
}
