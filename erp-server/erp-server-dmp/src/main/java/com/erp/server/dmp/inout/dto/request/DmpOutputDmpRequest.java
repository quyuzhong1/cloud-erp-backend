package com.erp.server.dmp.inout.dto.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;

import lombok.Data;

/**
 * 输出任务dmp状态请求参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputDmpRequest extends DmpOutputMongoRequest{
	/**
	 * dmp业务信息
	 */
	private Map<DmpCfgInputConvertEntity , List<BaseEntity>> convertInputDmpBaseEntityListMaps = new HashMap<>();
}
