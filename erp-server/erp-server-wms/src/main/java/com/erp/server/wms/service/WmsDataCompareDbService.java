package com.erp.server.wms.service;

import java.util.List;

import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareDTO;

public interface WmsDataCompareDbService<T extends DataCompareDTO> {
	List<T> getDataCompareByCondition(T params);
}
