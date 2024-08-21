package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.wms.entity.WmsLocalPushMessageEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputErpPushApiInitHandler extends DmpInputInitHandler{

	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();
		
        List<WmsLocalPushMessageEntity> list = FeignQuery.create(WmsLocalPushMessageEntity.class)
        		.eq(WmsLocalPushMessageEntity::getSourceType, apiType)
        		.ge(WmsLocalPushMessageEntity::getUpdateTime, dmpInputTaskEntity.getStartTime())
        		.le(WmsLocalPushMessageEntity::getUpdateTime, dmpInputTaskEntity.getEndTime())
        		.list();
		
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSON.toJSONString(list));
		
		return Collections.singletonList(dmpInputTaskInitDTO);
	}
	
}
