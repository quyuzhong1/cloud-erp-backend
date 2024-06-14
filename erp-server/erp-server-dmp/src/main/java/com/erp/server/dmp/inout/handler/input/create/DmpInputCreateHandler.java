package com.erp.server.dmp.inout.handler.input.create;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入创建任务处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public abstract class DmpInputCreateHandler extends DmpInputHandler{
	
	@Autowired
	private DmpCfgInputService dmpCfgInputService;
	
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
		
		if(StringUtils.isBlank(cfgInputId)) {
			log.warn("输入信息id为空");
			return;
		}
		DmpCfgInputEntity dmpCfgInputEntity = dmpCfgInputService.getById(cfgInputId);
		if(dmpCfgInputEntity == null) {
			log.warn("输入信息不存在id={}" , cfgInputId);
			return;
		}
		Boolean disabled = dmpCfgInputEntity.getDisabled();
		if(Boolean.TRUE.equals(disabled)) {
			log.warn("输入信息数据代码【{}】被禁用" , dmpCfgInputEntity.getCode());
			return;
		}
		
		dmpResponse.setDmpCfgInputEntity(dmpCfgInputEntity);
		List<DmpInputTaskEntity> befortDmpInputTaskEntityList = createInputTask(dmpRequest , dmpResponse);
		if(CollUtil.isEmpty(befortDmpInputTaskEntityList)) {
			return;
		}
		dmpResponse.setBeforeDmpInputTaskEntityList(befortDmpInputTaskEntityList);
		chain.doDmpHandler(dmpRequest, dmpResponse);
		
	}
	
	public abstract List<DmpInputTaskEntity> createInputTask(DmpInputCreateRequest dmpRequest, DmpInputCreateResponse dmpResponse);
}
