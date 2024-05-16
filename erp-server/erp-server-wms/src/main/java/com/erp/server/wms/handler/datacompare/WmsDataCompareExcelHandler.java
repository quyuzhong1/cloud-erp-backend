package com.erp.server.wms.handler.datacompare;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareDTO;

@Service
public class WmsDataCompareExcelHandler extends WmsAbstractDataCompareHandler{

	@Override
	protected <T extends DataCompareDTO> Class<T> getUploadDtoClass() {
		return null;
	}

	@Override
	protected List<Map<String, String>> getDbData(String systemDataCondition, String taskId) {
		return null;
	}

}
