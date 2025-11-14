package com.erp.server.dmp.inout.dto.response;

import com.erp.model.dmp.entity.DmpCfgEtlEntity;

import lombok.Data;

/**
 *  Etl创建任务响应参数
 * @author Administrator
 *
 */
@Data
public class DmpEtlCreateResponse extends DmpEtlResponse{
	private DmpCfgEtlEntity dmpCfgEtlEntity;
}
