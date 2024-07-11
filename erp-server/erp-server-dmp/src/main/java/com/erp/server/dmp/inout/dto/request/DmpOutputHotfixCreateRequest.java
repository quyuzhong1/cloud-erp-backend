package com.erp.server.dmp.inout.dto.request;

import java.util.List;

import com.common.business.wrapper.QueryParam;

import lombok.Data;

/**
 * 输出创建任务请求参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputHotfixCreateRequest extends DmpOutputCreateRequest{
	private List<QueryParam> queryParams;
}
