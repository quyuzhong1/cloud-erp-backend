package com.erp.server.dmp.inout.dto.response;

import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;

import lombok.Data;

/**
 * 输入任务响应参数
 * @author Administrator
 *
 */
@Data
public class DmpInputTaskResponse extends DmpInputResponse{
	
	/**
	 * 推送数据配置明细
	 * 可能为空，赋值在com.erp.server.dmp.inout.handler.input.task.DmpInputBaseTaskHandler.doDmpHandler(DmpInputTaskRequest, DmpInputTaskResponse, DmpHandlerChain)
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
	
}
