package com.erp.server.wms.handler.datacompare;

import org.springframework.stereotype.Service;

import com.erp.model.wms.dto.WmsDataCompareTaskDTO.OverseasInboundDTO;
import com.erp.server.wms.service.impl.OverseasWarehouseInboundServiceImpl;

@Service
public class WmsDataCompareOverHandler extends WmsAbstractDataCompareHandler<OverseasWarehouseInboundServiceImpl , OverseasInboundDTO>{

}
