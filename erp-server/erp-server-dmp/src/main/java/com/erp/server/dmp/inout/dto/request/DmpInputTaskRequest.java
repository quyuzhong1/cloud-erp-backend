package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

@Data
public class DmpInputTaskRequest extends DmpInputRequest{
	/**
	 * 输入信息任务id
	 */
	private String inputTaskId;
}
