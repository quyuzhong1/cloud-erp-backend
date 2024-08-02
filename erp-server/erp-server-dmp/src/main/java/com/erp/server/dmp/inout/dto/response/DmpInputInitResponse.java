package com.erp.server.dmp.inout.dto.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;

import lombok.Data;

/**
 *  输入任务init状态响应参数
 * @author Administrator
 *
 */
@Data
public class DmpInputInitResponse extends DmpInputTaskResponse{
	private Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = new HashMap<>();

	/**
	 * 执行输出handler
	 */
	private boolean doOutputChain = true;
	
	/**
	 * 执行子任务
	 */
	private boolean doChildCfgInput = true;

	/**
	 * 执行更新状态
	 */
	private boolean doUpdateStatus = true;
	
	
	/**
	 * 执行后续handler
	 */
	private boolean doNextChain = true;
	
	/**
	 * 执行后续状态handler
	 */
	private boolean doNextStatus = true;
}
