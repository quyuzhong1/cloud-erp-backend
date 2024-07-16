package com.erp.server.dmp.inout.handler.input.task.init.api;

import java.util.List;

import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 * @author Administrator
 *
 */
public interface DmpInputApiInitHandler {
	/**
	 * 获取api数据
	 * @param dmpInputApiInitRequest
	 * @return
	 */
	List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest);
}
