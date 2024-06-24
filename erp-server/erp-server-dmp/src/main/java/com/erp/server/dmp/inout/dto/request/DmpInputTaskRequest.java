package com.erp.server.dmp.inout.dto.request;

import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;

import lombok.Data;

@Data
public class DmpInputTaskRequest extends DmpInputRequest{
	/**
	 * 输入信息任务id
	 */
	private String inputTaskId;
	
	/**
	 * 处理的任务状态
	 */
	private DmpInputTaskStatusEnum dealTaskStatus;
}
