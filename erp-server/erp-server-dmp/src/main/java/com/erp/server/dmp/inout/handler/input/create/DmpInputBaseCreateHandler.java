package com.erp.server.dmp.inout.handler.input.create;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;
import com.erp.server.dmp.service.DmpCfgInputService;

import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入创建任务处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public abstract class DmpInputBaseCreateHandler extends DmpInputHandler{
	
	@Autowired
	private DmpCfgInputService dmpCfgInputService;
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputCreateRequest)) {
            throw new ServiceException(dmpRequest + " not DmpInputCreateRequest");
        }
        if (!(dmpResponse instanceof DmpInputCreateResponse)) {
            throw new ServiceException(dmpResponse + " not DmpInputResponse");
        }
        doDmpHandler((DmpInputCreateRequest) dmpRequest, (DmpInputCreateResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpInputCreateRequest dmpRequest, DmpInputCreateResponse dmpResponse, DmpHandlerChain chain) {
		String cfgInputId = dmpRequest.getCfgInputId();
		
		boolean throwException = dmpRequest.isThrowException();
		String msg = "";
		if(StringUtils.isBlank(cfgInputId)) {
			msg = "输入信息id为空";
			log.warn(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
			return;
		}
		DmpCfgInputEntity dmpCfgInputEntity = dmpCfgInputService.getById(cfgInputId);
		if(dmpCfgInputEntity == null) {
			msg = "输入信息不存在id=" + cfgInputId;
			log.warn(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
			return;
		}
		Boolean disabled = dmpCfgInputEntity.getDisabled();
		if(Boolean.TRUE.equals(disabled)) {
			msg = "输入信息数据代码【"+ dmpCfgInputEntity.getCode() +"】被禁用";
			log.warn(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
			return;
		}
		
		dmpResponse.setDmpCfgInputEntity(dmpCfgInputEntity);
		List<DmpInputTaskEntity> befortDmpInputTaskEntityList = this.createInputTask(dmpRequest , dmpResponse);
		dmpResponse.setBeforeDmpInputTaskEntityList(befortDmpInputTaskEntityList);
		chain.doDmpHandler(dmpRequest, dmpResponse);
		
	}
	
	public abstract List<DmpInputTaskEntity> createInputTask(DmpInputCreateRequest dmpRequest, DmpInputCreateResponse dmpResponse);
}
