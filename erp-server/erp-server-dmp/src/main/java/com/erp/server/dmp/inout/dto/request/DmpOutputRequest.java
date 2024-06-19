package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

@Data
public class DmpOutputRequest extends DmpRequest{
	/**
	 * 执行输出handler
	 */
	private boolean doNextChain = true;
}
