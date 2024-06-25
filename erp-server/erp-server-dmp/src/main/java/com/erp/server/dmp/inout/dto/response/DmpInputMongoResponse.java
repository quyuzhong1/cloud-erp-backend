package com.erp.server.dmp.inout.dto.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;

import lombok.Data;

/**
 *  输入任务mongo状态响应参数
 * @author Administrator
 *
 */
@Data
public class DmpInputMongoResponse extends DmpInputFdsResponse{
	/**
	 * mongo业务信息
	 */
	private Map<DmpCfgInputConvertEntity , List<Map<String, Object>>> convertInputMongoEntityListMaps = new HashMap<>();
}
