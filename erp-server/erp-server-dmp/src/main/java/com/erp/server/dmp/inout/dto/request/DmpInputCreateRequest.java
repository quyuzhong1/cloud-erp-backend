package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

@Data
public class DmpInputCreateRequest extends DmpInputRequest{
	/**
	 * 输入信息id
	 */
	private String cfgInputId;
}
