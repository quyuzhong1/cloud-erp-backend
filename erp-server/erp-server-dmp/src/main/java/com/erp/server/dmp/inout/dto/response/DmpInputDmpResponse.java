package com.erp.server.dmp.inout.dto.response;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;

import lombok.Data;

/**
 *  输入任务dmp状态响应参数
 * @author Administrator
 *
 */
@Data
public class DmpInputDmpResponse extends DmpInputMongoResponse{
	/**
	 * dmp业务信息
	 */
	private Map<DmpCfgInputConvertEntity , List<BaseEntity>> convertInputDmpBaseEntityListMaps = new HashMap<>();
	
	/**
	 * 变动的dmp业务信息
	 */
	private Map<DmpCfgInputConvertEntity , List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = new HashMap<>();
	
	/**
	 * 删除的dmp业务信息
	 */
	private Map<DmpCfgInputConvertEntity , Set<String>> deleteConvertInputDmpBaseEntityMaps = new HashMap<>();
}
