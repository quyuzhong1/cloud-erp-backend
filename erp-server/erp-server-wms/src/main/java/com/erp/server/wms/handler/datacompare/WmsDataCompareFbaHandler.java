package com.erp.server.wms.handler.datacompare;

import org.springframework.stereotype.Service;

import com.erp.model.wms.dto.WmsDataCompareTaskDTO.FbaShipmentDTO;
import com.erp.server.wms.service.impl.FbaShipmentServiceImpl;

@Service
public class WmsDataCompareFbaHandler extends WmsAbstractDataCompareHandler<FbaShipmentServiceImpl , FbaShipmentDTO>{

}
