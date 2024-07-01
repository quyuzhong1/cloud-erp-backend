package com.erp.server.dmp.inout.dto.response;

import com.erp.model.dmp.entity.DmpCfgOutputEntity;

import lombok.Data;

/**
 * 输出創建任务响应参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputCreateResponse extends DmpOutputResponse{
	private DmpCfgOutputEntity dmpCfgOutputEntity;
}
