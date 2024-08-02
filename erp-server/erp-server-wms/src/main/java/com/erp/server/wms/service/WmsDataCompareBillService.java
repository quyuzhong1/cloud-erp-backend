package com.erp.server.wms.service;

import java.util.List;
import java.util.Map;

import com.erp.model.wms.entity.WmsDataCompareTaskEntity;

/**
 * 数据对比单据服务类
 * @author Administrator
 *
 */
public interface WmsDataCompareBillService {

	List<Map<String, String>> getSystemData(WmsDataCompareTaskEntity wmsDataCompareTaskEntity);
	
	Integer getDbSystemDataCount(String systemDataCondition);
	
	String uploadSystemDataByCondition(WmsDataCompareTaskEntity wmsDataCompareTaskEntity);
	
	void setBillType(String billType);
}
