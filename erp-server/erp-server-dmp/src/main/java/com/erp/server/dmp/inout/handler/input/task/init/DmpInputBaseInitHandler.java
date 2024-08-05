package com.erp.server.dmp.inout.handler.input.task.init;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.erp.server.dmp.inout.dto.request.*;
import com.erp.server.dmp.inout.handler.input.task.init.api.mabang.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpCfgInputTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputKingdeeApiInitHandler;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputWdtApiInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpCfgApiService;

import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputBaseInitHandler extends DmpInputInitHandler{

	@Autowired
	private DmpCfgApiService dmpCfgApiService;
	
	@Autowired
	private MongoService mongoService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		String type = dmpCfgInputEntity.getType();
		String typeId = dmpCfgInputEntity.getTypeId();
		if(DmpCfgInputTypeEnum.API.getCode().equals(type)) {
			DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
			String apiClass = dmpCfgApiEntity.getApiClass();
			DmpInputApiInitHandler dmpInputApiInitHandler = null;
			DmpInputApiInitRequest dmpInputApiInitRequest = null;
			
			LocalDateTime startTime = dmpInputTaskEntity.getStartTime();
			LocalDateTime endTime = dmpInputTaskEntity.getEndTime();
			
			String parentStorageName = "";
			
			String extendJson = dmpCfgInputEntity.getExtendJson();
			if(DmpBasicSystemCodeEnum.KINGDEE.getCode().equals(dmpBasicSystemEntity.getCode())) {
				dmpInputApiInitHandler = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(apiClass), DmpInputKingdeeApiInitHandler.class);
				DmpInputKingdeeApiInitRequest dmpInputKingdeeApiInitRequest = new DmpInputKingdeeApiInitRequest();
				dmpInputApiInitRequest = dmpInputKingdeeApiInitRequest;
				dmpInputKingdeeApiInitRequest.setFormId(dmpCfgApiEntity.getApiType());
				if(StringUtils.isNotBlank(extendJson)) {
					DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
					JSONObject parseObject = JSON.parseObject(extendJson);
					if("orderItem".equals(dmpCfgInputEntity.getCode())) {
						if(StringUtils.isNotBlank(nextLevelId)) {
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
							dmpInputKingdeeApiInitRequest.setFilterStr(parseObject.getString("filterStr")
									.replace("{FBillNo}", findMongoDataById.get("FBillNo").toString())
									.replace("{FID}", findMongoDataById.get("FID").toString()));
						}else {
							List<Map<String, Object>> findMongoData = null;
							parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
							if(StringUtils.isNotBlank(parentStorageName)) {
								List<ParamData> paramDataList = new ArrayList<>();
								paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
								findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
							}
							if(findMongoData == null) {
								return new ArrayList<>();
							}
							
							dmpInputKingdeeApiInitRequest.setFilterStr(parseObject.getString("filterStr")
									.replace("{FBillNo}", findMongoData.stream().map(f -> f.get("FBillNo").toString()).collect(Collectors.joining("','")))
									.replace("{FID}", findMongoData.stream().map(f -> f.get("FID").toString()).collect(Collectors.joining("','"))));
						}
					}else {
						dmpInputKingdeeApiInitRequest.setFilterStr(parseObject.getString("filterStr")
								.replace("{startTime}", sdf.format(startTime))
								.replace("{endTime}", sdf.format(endTime)));
					}
					
					dmpInputKingdeeApiInitRequest.setFieldKeys(parseObject.getString("fieldKeys"));
				}
			} else {
				dmpInputApiInitHandler = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(apiClass), DmpInputApiInitHandler.class);
				dmpInputApiInitRequest = new DmpInputApiInitRequest();
				dmpInputApiInitRequest.setRequestParam(extendJson);
				dmpInputApiInitRequest.setNextLevelId(nextLevelId);
				dmpInputApiInitRequest.setApiType(dmpCfgApiEntity.getApiType());
			}
			
			dmpInputApiInitRequest.setStartTime(startTime);
			dmpInputApiInitRequest.setEndTime(endTime);
			
			return dmpInputApiInitHandler.getApiData(dmpInputApiInitRequest);
		}else if(DmpCfgInputTypeEnum.DB.getCode().equals(type)) {
			
		}else if(DmpCfgInputTypeEnum.MQ.getCode().equals(type)) {
			
		}
		return null;
	}
	
}
