package com.erp.server.dmp.inout.dto.base;

import com.common.core.entity.BaseEntity;

import lombok.Data;

@Data
public class DmpInputDmpBaseEntity extends BaseEntity<DmpInputDmpBaseEntity> {
	/**
	 * 任务id
	 */
	private String inputTaskId;
	
	/**
	 * 文件id
	 */
	private String fileId;
	
	/**
	 * 下一层级id
	 */
	private String nextLevelId;
}
