package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

/**
 * 输出创建任务请求参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputCreateRequest extends DmpOutputRequest{
	/**
	 * 输出信息id
	 */
	private String cfgOutputId;
	
	/**
	 * 是否抛出异常
	 */
	private boolean isThrowException = false;
	
	/**
	 * 是否校验黑名单
	 */
	private boolean isNotValidate = false;
	
	/**
	 * 是否重推
	 */
	private boolean isRetryPush = false;
}
