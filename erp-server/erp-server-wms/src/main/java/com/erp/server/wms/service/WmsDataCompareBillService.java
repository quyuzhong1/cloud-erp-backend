package com.erp.server.wms.service;

import java.util.List;

/**
 * 数据对比单据服务类
 * @author Administrator
 *
 */
public interface WmsDataCompareBillService {

	List<?> getDataCompareByCondition(String systemDataCondition);
	
	Integer getSystemDataCount(String systemDataCondition);
	
	String uploadSystemDataByCondition(String systemDataCondition);
}
