package com.erp.server.wms.handler.datacompare;

import org.springframework.stereotype.Service;

import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SoOutstockDTO;
import com.erp.server.wms.service.impl.SoOutstockServiceImpl;

@Service
public class WmsDataCompareSoHandler extends WmsAbstractDataCompareHandler<SoOutstockServiceImpl , SoOutstockDTO>{

}
