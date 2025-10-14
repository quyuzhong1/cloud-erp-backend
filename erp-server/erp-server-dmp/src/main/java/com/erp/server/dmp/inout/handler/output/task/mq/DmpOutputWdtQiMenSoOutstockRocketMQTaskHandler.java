package com.erp.server.dmp.inout.handler.output.task.mq;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.business.dto.WdtSoOutStockDTO;
import com.erp.model.dmp.entity.DmpSoOutstockDetailEntity;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;

@Service
@Scope("prototype")
public class DmpOutputWdtQiMenSoOutstockRocketMQTaskHandler extends DmpOutputWdtSoOutstockRocketMQTaskHandler{

	/**
     * 解析订单数据
     **/
	@Override
    public WdtSoOutStockDTO initOrderInfoEntity(DmpSoOutstockEntity entity , List<DmpSoOutstockDetailEntity> itemList , String cfgOutputId) {
    	WdtSoOutStockDTO resultEntity = super.initOrderInfoEntity(entity, itemList, cfgOutputId);
    	if(resultEntity != null) {
    		resultEntity.setCreateUserName("qimen");
			resultEntity.setCreateUserId("1808810116456153089");
    	}
        return resultEntity;
    }

}
