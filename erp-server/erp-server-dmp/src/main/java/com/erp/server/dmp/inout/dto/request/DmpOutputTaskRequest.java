package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

/**
 * 输出任务状态请求参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputTaskRequest extends DmpOutputRequest{
	/**
	 * 输入信息任务id
	 */
	private String inputTaskId;
}
