package com.erp.server.dmp.inout.dto.response;

import java.util.List;

import com.erp.model.dmp.entity.DmpOutputTaskEntity;

import lombok.Data;

/**
 * 输出响应参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputResponse extends DmpResponse{
	/**
	 *处理前输出任务信息
	 */
	private List<DmpOutputTaskEntity> beforeDmpOutputTaskEntityList;
	/**
	 *处理前输出任务信息
	 */
	private List<DmpOutputTaskEntity> afterDmpOutputTaskEntityList;
}
