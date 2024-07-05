package com.erp.server.dmp.inout.handler.output.create;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputCreateResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * dmp输出任务创建处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpOutputHotfixCreateHandler extends DmpOutputBaseCreateHandler{
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<DmpOutputTaskEntity> createOutputTask(DmpOutputCreateRequest dmpRequest,
			DmpOutputCreateResponse dmpResponse) {
		return null;
	}
}
