package com.erp.server.wms.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.erp.model.wms.enums.WmsDataCompareTaskBillTypeEnum;
import com.erp.server.wms.handler.datacompare.WmsDataCompareFbaHandler;
import com.erp.server.wms.handler.datacompare.WmsDataCompareOverHandler;
import com.erp.server.wms.handler.datacompare.WmsDataCompareSoHandler;
import com.erp.server.wms.service.WmsDataCompareBillService;

@Component
public class WmsDataCompareHandlerFactory {
	private Map<String, WmsDataCompareBillService<?>> wmsDataCompareBillHandlerMap = new HashMap<>();
	
	@Autowired
	private WmsDataCompareSoHandler wmsDataCompareSoHandler;
	
	@Autowired
	private WmsDataCompareFbaHandler wmsDataCompareFbaHandler;
	
	@Autowired
	private WmsDataCompareOverHandler wmsDataCompareOverHandler;
	
	@PostConstruct
    public void init() {
		putHandler(WmsDataCompareTaskBillTypeEnum.SOOUTSTOCK.getCode(), wmsDataCompareSoHandler);
		putHandler(WmsDataCompareTaskBillTypeEnum.FBASHIPMENT.getCode(), wmsDataCompareFbaHandler);
		putHandler(WmsDataCompareTaskBillTypeEnum.OVERSEASINBOUND.getCode(), wmsDataCompareOverHandler);
	}
	
	public WmsDataCompareBillService<?> get(String billType) {
        return wmsDataCompareBillHandlerMap.get(billType);
    }
	
	public void putHandler(String billType , WmsDataCompareBillService<?> wmsDataCompareBillService) {
		wmsDataCompareBillHandlerMap.put(billType, wmsDataCompareBillService);
		wmsDataCompareBillService.setBillType(billType);
	}
}
