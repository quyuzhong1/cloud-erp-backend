package com.erp.server.dmp.inout.dto.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;

import lombok.Data;

/**
 * 输出任务mongo状态请求参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputMongoRequest extends DmpOutputFdsRequest{
	/**
	 * mongo业务信息
	 */
	private Map<DmpCfgInputConvertEntity , List<Map<String, Object>>> convertInputMongoEntityListMaps = new HashMap<>();
}
