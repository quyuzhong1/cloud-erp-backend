package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpCfgInputTypeEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputKingdeeApiInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputKingdeeApiInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpCfgApiService;

@Service
@Scope("prototype")
public class DmpInputTypeInitHandler extends DmpInputInitHandler{

	@Autowired
	private DmpCfgApiService dmpCfgApiService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		DmpCfgInputEntity dmpCfgInputEntity = dmpResponse.getDmpCfgInputEntity();
		DmpBasicSystemEntity dmpBasicSystemEntity = dmpResponse.getDmpBasicSystemEntity();
		String type = dmpCfgInputEntity.getType();
		String typeId = dmpCfgInputEntity.getTypeId();
		if(DmpCfgInputTypeEnum.API.getCode().equals(type)) {
			DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
			String apiClass = dmpCfgApiEntity.getApiClass();
			DmpInputApiInitHandler dmpInputApiInitHandler = null;
			DmpInputKingdeeApiInitRequest dmpInputApiInitRequest = null;
			if(DmpBasicSystemCodeEnum.KINGDEE.getCode().equals(dmpBasicSystemEntity.getCode())) {
				dmpInputApiInitHandler = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(apiClass), DmpInputKingdeeApiInitHandler.class);
				dmpInputApiInitRequest = new DmpInputKingdeeApiInitRequest();
				dmpInputApiInitRequest = (DmpInputKingdeeApiInitRequest) dmpInputApiInitRequest;
				dmpInputApiInitRequest.setFormId(dmpCfgApiEntity.getApiType());
//				dmpInputApiInitRequest.setFormId(PlatformApiEnum.SAL_SALEORDER.getTaskName());
			}
			DmpInputTaskEntity dmpInputTaskEntity = dmpResponse.getBeforeDmpInputTaskEntityList().get(0);
			dmpInputApiInitRequest.setStartTime(dmpInputTaskEntity.getStartTime());
			dmpInputApiInitRequest.setEndTime(dmpInputTaskEntity.getEndTime());
			
			return dmpInputApiInitHandler.getApiData(dmpInputApiInitRequest);
		}else if(DmpCfgInputTypeEnum.DB.getCode().equals(type)) {
			
		}else if(DmpCfgInputTypeEnum.MQ.getCode().equals(type)) {
			
		}
		return null;
	}

}
