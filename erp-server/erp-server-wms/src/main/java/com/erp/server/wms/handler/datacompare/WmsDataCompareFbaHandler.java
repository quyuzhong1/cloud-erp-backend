package com.erp.server.wms.handler.datacompare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.model.wms.dto.WmsDataCompareTaskDTO.FbaShipmentDTO;
import com.erp.model.wms.enums.WmsDataCompareTaskBillTypeEnum;
import com.erp.server.wms.service.WmsDataCompareDbService;
import com.erp.server.wms.service.impl.FbaShipmentServiceImpl;

@Service
public class WmsDataCompareFbaHandler extends WmsAbstractDataCompareHandler<FbaShipmentDTO>{
	@Autowired
    private FbaShipmentServiceImpl fbaShipmentServiceImpl;

	@Override
	WmsDataCompareDbService<FbaShipmentDTO> getWmsDataCompareDbService() {
		return fbaShipmentServiceImpl;
	}

	@Override
	WmsDataCompareTaskBillTypeEnum getWmsDataCompareTaskBillTypeEnum() {
		return WmsDataCompareTaskBillTypeEnum.FBASHIPMENT;
	}

}
