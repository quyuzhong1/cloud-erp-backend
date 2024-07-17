package com.erp.server.dmp.inout.handler.output.create;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.output.DmpOutputHandler;
import com.erp.server.dmp.service.DmpCfgOutputService;

import lombok.extern.slf4j.Slf4j;

/**
 * dmp输出任务创建处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public abstract class DmpOutputBaseCreateHandler extends DmpOutputHandler{
	
	@Autowired
	private DmpCfgOutputService dmpCfgOutputService;
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void doDmpHandler(DmpOutputRequest dmpRequest, DmpOutputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpOutputCreateRequest)) {
            throw new ServiceException(dmpRequest + " not DmpOutputCreateRequest");
        }
        if (!(dmpResponse instanceof DmpOutputCreateResponse)) {
            throw new ServiceException(dmpResponse + " not DmpOutputCreateResponse");
        }
        doDmpHandler((DmpOutputCreateRequest) dmpRequest, (DmpOutputCreateResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpOutputCreateRequest dmpRequest, DmpOutputCreateResponse dmpResponse, DmpHandlerChain chain) {
		String cfgOutputId = dmpRequest.getCfgOutputId();
		boolean throwException = dmpRequest.isThrowException();
		String msg = "";
		if(StringUtils.isBlank(cfgOutputId)) {
			msg = "输出信息id为空";
			log.warn(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
			return;
		}
		
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpCfgOutputService.getById(cfgOutputId);
		if(dmpCfgOutputEntity == null) {
			msg = "输出信息不存在id=" + cfgOutputId;
			log.warn(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
			return;
		}
		
		Boolean disabled = dmpCfgOutputEntity.getDisabled();
		if(Boolean.TRUE.equals(disabled)) {
			msg = "输出信息数据id【"+ cfgOutputId +"】被禁用";
			log.warn(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
			return;
		}
		
		dmpResponse.setDmpCfgOutputEntity(dmpCfgOutputEntity);
		List<DmpOutputTaskEntity> beforeDmpOutputTaskEntityList = this.createOutputTask(dmpRequest, dmpResponse);
		dmpResponse.setBeforeDmpOutputTaskEntityList(beforeDmpOutputTaskEntityList);
		dmpResponse.setAfterDmpOutputTaskEntityList(beforeDmpOutputTaskEntityList);
		chain.doDmpHandler(dmpRequest, dmpResponse);
		
	}
	
	public abstract List<DmpOutputTaskEntity> createOutputTask(DmpOutputCreateRequest dmpRequest, DmpOutputCreateResponse dmpResponse);
}
