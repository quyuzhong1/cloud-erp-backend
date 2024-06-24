package com.erp.server.dmp.inout.dto.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;

import lombok.Data;

@Data
public class DmpOutputDmpRequest extends DmpOutputMongoRequest{
	/**
	 * dmp业务信息
	 */
	private Map<DmpCfgInputConvertEntity , List<BaseEntity>> convertInputDmpBaseEntityListMaps = new HashMap<>();
}
