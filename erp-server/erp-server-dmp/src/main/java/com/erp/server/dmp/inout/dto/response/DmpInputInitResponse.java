package com.erp.server.dmp.inout.dto.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;

import lombok.Data;

@Data
public class DmpInputInitResponse extends DmpInputTaskResponse{
	private Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = new HashMap<>();
	
	/**
	 * 执行更新状态
	 */
	private boolean doUpdateStatus = true;
}
