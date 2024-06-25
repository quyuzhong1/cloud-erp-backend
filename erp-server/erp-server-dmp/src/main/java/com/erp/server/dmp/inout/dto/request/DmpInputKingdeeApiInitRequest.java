package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

/**
 * 输入任务init状态api类型金蝶请求参数
 * @author Administrator
 *
 */
@Data
public class DmpInputKingdeeApiInitRequest extends DmpInputApiInitRequest{
	private String filterStr;
	private String fieldKeys;
	private String formId;
}
