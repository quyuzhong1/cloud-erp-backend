package com.erp.server.dmp.inout.dto.response;

import java.util.List;

import com.erp.model.dmp.entity.DmpInputTaskEntity;

import lombok.Data;

@Data
public class DmpInputResponse extends DmpResponse{
	
	/**
	 *处理前输入任务信息
	 */
	private List<DmpInputTaskEntity> beforeDmpInputTaskEntityList;
	/**
	 *处理前输入任务信息
	 */
	private List<DmpInputTaskEntity> afterDmpInputTaskEntityList;
	
}
