package com.erp.server.dmp.inout.handler.input.create;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;

/**
 * dmp输入创建历史任务处理器
 * @author Administrator
 *
 */
@Service
public class DmpInputHistoryCreateHandler extends DmpInputDetailCreateHandler{
	@Override
	public DmpInputTaskTaskTypeEnum getDmpInputTaskTaskTypeEnum() {
		return DmpInputTaskTaskTypeEnum.HISTORY;
	}
	
	@Override
	public Set<DmpInputTaskTaskTypeEnum> getIngTaskType() {
		Set<DmpInputTaskTaskTypeEnum> set = new HashSet<>();
		set.add(DmpInputTaskTaskTypeEnum.NORMAL);
		set.add(DmpInputTaskTaskTypeEnum.COMPENSATE);
		set.add(DmpInputTaskTaskTypeEnum.HISTORY);
		return set;
	}
}
