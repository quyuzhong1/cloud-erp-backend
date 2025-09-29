package com.erp.server.dmp.inout.dto.response;

import java.util.List;

import com.erp.model.dmp.entity.DmpEtlTaskEntity;

import lombok.Data;

/**
 * Etl响应参数
 * @author Administrator
 *
 */
@Data
public class DmpEtlResponse extends DmpResponse{
	
	/**
	 *处理前Etl任务信息
	 */
	private List<DmpEtlTaskEntity> beforeDmpEtlTaskEntityList;
	/**
	 *处理前Etl任务信息
	 */
	private List<DmpEtlTaskEntity> afterDmpEtlTaskEntityList;
	
}
