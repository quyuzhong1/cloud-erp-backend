package com.erp.server.dmp.inout.dto.response;

import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;

import lombok.Data;

@Data
public class DmpInputTaskResponse extends DmpInputResponse{
	
	/**
	 * 推送数据配置明细
	 */
	private DmpCfgInputDetailEntity dmpCfgInputDetailEntity;
	
	/**
	 * 推送数据配置
	 */
	private DmpCfgInputEntity dmpCfgInputEntity;
	
	/**
	 * 外部系统
	 */
	private DmpBasicSystemEntity dmpBasicSystemEntity;
	
	/**
	 * 执行输出handler
	 */
	private boolean doOutputChain = true;
	
	/**
	 * 执行后续handler
	 */
	private boolean doNextChain = true;
}
