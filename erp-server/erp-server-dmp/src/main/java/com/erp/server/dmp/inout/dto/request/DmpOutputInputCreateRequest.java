package com.erp.server.dmp.inout.dto.request;

import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;

import lombok.Data;

/**
 * 输出创建任务请求参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputInputCreateRequest extends DmpOutputCreateRequest{
	private String inputTaskId;
	
	private DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity;
	
	private DmpOutputTaskRequest dmpOutputTaskRequest;
}
