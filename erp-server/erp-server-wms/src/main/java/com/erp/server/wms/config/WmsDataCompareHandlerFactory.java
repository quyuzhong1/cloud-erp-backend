package com.erp.server.wms.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.erp.model.wms.enums.WmsDataCompareTaskBillTypeEnum;
import com.erp.server.wms.handler.datacompare.WmsDataCompareExcelHandler;
import com.erp.server.wms.handler.datacompare.WmsDataCompareFbaHandler;
import com.erp.server.wms.handler.datacompare.WmsDataCompareOverHandler;
import com.erp.server.wms.handler.datacompare.WmsDataCompareSoHandler;
import com.erp.server.wms.service.WmsDataCompareBillService;

@Component
public class WmsDataCompareHandlerFactory {
	private Map<String, WmsDataCompareBillService> wmsDataCompareBillHandlerMap = new HashMap<>();
	
	@Resource
	private WmsDataCompareExcelHandler wmsDataCompareExcelHandler;

	@Resource
	private WmsDataCompareSoHandler wmsDataCompareSoHandler;
	
	@Resource
	private WmsDataCompareFbaHandler wmsDataCompareFbaHandler;
	
	@Resource
	private WmsDataCompareOverHandler wmsDataCompareOverHandler;
	
	@PostConstruct
    public void init() {
		putHandler(null, wmsDataCompareExcelHandler);
		putHandler("", wmsDataCompareExcelHandler);
		putHandler(WmsDataCompareTaskBillTypeEnum.SOOUTSTOCK.getCode(), wmsDataCompareSoHandler);
		putHandler(WmsDataCompareTaskBillTypeEnum.FBASHIPMENT.getCode(), wmsDataCompareFbaHandler);
		putHandler(WmsDataCompareTaskBillTypeEnum.OVERSEASINBOUND.getCode(), wmsDataCompareOverHandler);
	}
	
	public WmsDataCompareBillService get(String billType) {
        return wmsDataCompareBillHandlerMap.get(billType);
    }
	
	public void putHandler(String billType , WmsDataCompareBillService wmsDataCompareBillService) {
		wmsDataCompareBillHandlerMap.put(billType, wmsDataCompareBillService);
		wmsDataCompareBillService.setBillType(billType);
	}
	
}
