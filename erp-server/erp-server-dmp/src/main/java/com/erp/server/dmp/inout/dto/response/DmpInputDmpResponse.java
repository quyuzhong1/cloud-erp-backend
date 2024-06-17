package com.erp.server.dmp.inout.dto.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;

import lombok.Data;

@Data
public class DmpInputDmpResponse extends DmpInputMongoResponse{
	/**
	 * dmp业务信息
	 */
	private Map<DmpCfgInputConvertEntity , List<BaseEntity>> convertInputDmpBaseEntityListMaps = new HashMap<>();
}
