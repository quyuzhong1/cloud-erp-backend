package com.erp.server.dmp.inout.handler.input.create;

import org.springframework.stereotype.Service;

import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;

/**
 * dmp输入创建正常任务处理器
 * @author Administrator
 *
 */
@Service
public class DmpInputNormalCreateHandler extends DmpInputDetailCreateHandler{
	@Override
	public DmpInputTaskTaskTypeEnum getDmpInputTaskTaskTypeEnum() {
		return DmpInputTaskTaskTypeEnum.NORMAL;
	}
	
}
