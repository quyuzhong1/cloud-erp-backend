package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

@Data
public class DmpOutputTaskRequest extends DmpOutputRequest{
	/**
	 * 输入信息任务id
	 */
	private String inputTaskId;
}
