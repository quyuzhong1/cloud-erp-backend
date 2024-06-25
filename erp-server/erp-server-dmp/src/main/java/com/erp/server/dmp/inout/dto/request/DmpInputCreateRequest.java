package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

/**
 * 输入创建任务请求参数
 * @author Administrator
 *
 */
@Data
public class DmpInputCreateRequest extends DmpInputRequest{
	/**
	 * 输入信息id
	 */
	private String cfgInputId;
	
	/**
	 * 是否抛出异常
	 */
	private boolean isThrowException = false;
}
