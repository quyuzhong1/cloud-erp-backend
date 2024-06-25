package com.erp.server.dmp.inout.dto.response;

import com.erp.model.dmp.entity.DmpCfgInputEntity;

import lombok.Data;

/**
 *  输入创建任务响应参数
 * @author Administrator
 *
 */
@Data
public class DmpInputCreateResponse extends DmpInputResponse{
	private DmpCfgInputEntity dmpCfgInputEntity;
}
