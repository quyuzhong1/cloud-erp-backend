package com.erp.server.wms.handler.datacompare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SoOutstockDTO;
import com.erp.model.wms.enums.WmsDataCompareTaskBillTypeEnum;
import com.erp.server.wms.service.WmsDataCompareDbService;
import com.erp.server.wms.service.impl.SoOutstockServiceImpl;

@Service
public class WmsDataCompareSoHandler extends WmsAbstractDataCompareHandler<SoOutstockDTO>{
	@Autowired
    private SoOutstockServiceImpl soOutstockServiceImpl;

	@Override
	WmsDataCompareDbService<SoOutstockDTO> getWmsDataCompareDbService() {
		return soOutstockServiceImpl;
	}

	@Override
	WmsDataCompareTaskBillTypeEnum getWmsDataCompareTaskBillTypeEnum() {
		return WmsDataCompareTaskBillTypeEnum.SOOUTSTOCK;
	}

}
