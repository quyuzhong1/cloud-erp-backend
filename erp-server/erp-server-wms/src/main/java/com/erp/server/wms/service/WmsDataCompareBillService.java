package com.erp.server.wms.service;

import java.util.List;

import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareDTO;

/**
 * 数据对比单据服务类
 * @author Administrator
 *
 */
public interface WmsDataCompareBillService<T extends DataCompareDTO> {

	List<T> getDataCompareByCondition(String systemDataCondition , String taskId);
	
	Integer getSystemDataCount(String systemDataCondition);
	
	String uploadSystemDataByCondition(String systemDataCondition);
	
	void setBillType(String billType);
	
	T getCompareDTO(String dtoJson);
}
