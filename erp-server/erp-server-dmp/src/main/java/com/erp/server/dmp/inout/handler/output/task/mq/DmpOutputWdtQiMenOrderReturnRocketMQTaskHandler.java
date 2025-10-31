package com.erp.server.dmp.inout.handler.output.task.mq;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.business.dto.WdtReturnOrderDTO;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;

@Service
@Scope("prototype")
public class DmpOutputWdtQiMenOrderReturnRocketMQTaskHandler extends DmpOutputWdtOrderReturnRocketMQTaskHandler{
	@Override
	public WdtReturnOrderDTO initOrderInfoEntity(DmpSoReturnInfoEntity entity, List<DmpSoReturnDetailEntity> itemList,
			String cfgOutputId) {
		WdtReturnOrderDTO resultEntity = super.initOrderInfoEntity(entity, itemList, cfgOutputId);
		if(resultEntity != null) {
			resultEntity.setApproveUserName("qimen");
    		resultEntity.setCreateUserName("qimen");
			resultEntity.setCreateUserId("1808810116456153089");
			resultEntity.setApproveUserId("1808810116456153089");
    	}
		return resultEntity;
	}
}
