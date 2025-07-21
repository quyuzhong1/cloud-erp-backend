package com.erp.server.dmp.inout.dto.request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;

import lombok.Data;

/**
 * 输出任务状态请求参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputTaskRequest extends DmpOutputRequest{
	/**
	 * 输出信息任务id
	 */
	private String outputTaskId;
	
	/**
	    * 执行超时时间，单位秒
	*/
	private Integer execTimeout;
	
	private Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = new HashMap<>();
	
	/**
	 * 文件上传信息
	 */
	private Map<DmpCfgInputConvertEntity , List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = new HashMap<>();
	
	/**
	 * mongo业务信息
	 */
	private Map<DmpCfgInputConvertEntity , List<Map<String, Object>>> convertInputMongoEntityListMaps = new HashMap<>();
	
	/**
	 * 变动的mongo业务信息
	 */
	private Map<DmpCfgInputConvertEntity , List<Map<String, Object>>> changeConvertInputMongoEntityListMaps = new HashMap<>();
	
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
	
	/**
	 * 是否校验数据
	 */
	private boolean isNotValidate = false;
	
	/**
	 * 是否重推
	 */
	private boolean isRetryPush = false;
	
}
