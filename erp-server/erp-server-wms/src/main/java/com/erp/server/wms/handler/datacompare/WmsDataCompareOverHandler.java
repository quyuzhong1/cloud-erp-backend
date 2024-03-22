package com.erp.server.wms.handler.datacompare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.model.wms.dto.WmsDataCompareTaskDTO.OverseasInboundDTO;
import com.erp.model.wms.enums.WmsDataCompareTaskBillTypeEnum;
import com.erp.server.wms.service.WmsDataCompareDbService;
import com.erp.server.wms.service.impl.OverseasWarehouseInboundServiceImpl;

@Service
public class WmsDataCompareOverHandler extends WmsAbstractDataCompareHandler<OverseasInboundDTO>{
	@Autowired
    private OverseasWarehouseInboundServiceImpl overseasWarehouseInboundServiceImpl;

	@Override
	WmsDataCompareDbService<OverseasInboundDTO> getWmsDataCompareDbService() {
		return overseasWarehouseInboundServiceImpl;
	}

	@Override
	WmsDataCompareTaskBillTypeEnum getWmsDataCompareTaskBillTypeEnum() {
		return WmsDataCompareTaskBillTypeEnum.OVERSEASINBOUND;
	}

}
