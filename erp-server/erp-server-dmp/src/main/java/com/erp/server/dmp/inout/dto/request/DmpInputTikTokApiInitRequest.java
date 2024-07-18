package com.erp.server.dmp.inout.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 输入任务init状态api类型金蝶请求参数
 * @author Administrator
 *
 */
@Data
public class DmpInputTikTokApiInitRequest extends DmpInputApiInitRequest{

	private String nextLevelId;

	private List<String> orderIds;

	private String apiType;
}
