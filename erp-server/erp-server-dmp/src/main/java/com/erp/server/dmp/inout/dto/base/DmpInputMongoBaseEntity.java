package com.erp.server.dmp.inout.dto.base;

import lombok.Data;

@Data
public class DmpInputMongoBaseEntity{
	/**
	 * id
	 */
	private String id;
	
	/**
	 * 版本
	 */
	private Integer version;
	
	/**
	 * 是否删除
	 */
	private Boolean isDeleted;
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
