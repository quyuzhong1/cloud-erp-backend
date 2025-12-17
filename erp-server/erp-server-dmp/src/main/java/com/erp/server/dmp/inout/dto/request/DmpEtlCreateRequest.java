package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

/**
 * Etl创建任务请求参数
 * @author Administrator
 *
 */
@Data
public class DmpEtlCreateRequest extends DmpEtlRequest{
	/**
	 * Etl信息id
	 */
	private String cfgEtlId;
	
	/**
	 * 是否抛出异常
	 */
	private boolean isThrowException = false;
}
