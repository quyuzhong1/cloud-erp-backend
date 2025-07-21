package com.erp.server.dmp.inout.handler.etl.create;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgEtlEntity;
import com.erp.model.dmp.entity.DmpEtlTaskEntity;
import com.erp.server.dmp.inout.dto.request.DmpEtlCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpEtlRequest;
import com.erp.server.dmp.inout.dto.response.DmpEtlCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpEtlResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.etl.DmpEtlHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmpEtl任务创建处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public abstract class DmpEtlBaseCreateHandler extends DmpEtlHandler{
	
	@Autowired
	private DmpHandlerCache dmpHandlerCache;
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void doDmpHandler(DmpEtlRequest dmpRequest, DmpEtlResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpEtlCreateRequest)) {
            throw new ServiceException(dmpRequest + " not DmpEtlCreateRequest");
        }
        if (!(dmpResponse instanceof DmpEtlCreateResponse)) {
            throw new ServiceException(dmpResponse + " not DmpEtlResponse");
        }
        doDmpHandler((DmpEtlCreateRequest) dmpRequest, (DmpEtlCreateResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpEtlCreateRequest dmpRequest, DmpEtlCreateResponse dmpResponse, DmpHandlerChain chain) {
		String cfgEtlId = dmpRequest.getCfgEtlId();
		
		boolean throwException = dmpRequest.isThrowException();
		String msg = "";
		if(StringUtils.isBlank(cfgEtlId)) {
			msg = "Etl信息id为空";
			log.warn(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
			return;
		}
		List<DmpCfgEtlEntity> dmpCfgEtlEntityList = dmpHandlerCache.getDmpCfgEtlEntityList(d -> d.getId().equals(cfgEtlId));
		if(CollUtil.isEmpty(dmpCfgEtlEntityList)) {
			msg = "Etl信息不存在id=" + cfgEtlId;
			log.warn(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
			return;
		}
		DmpCfgEtlEntity dmpCfgEtlEntity = dmpCfgEtlEntityList.get(0);
		
		Boolean disabled = dmpCfgEtlEntity.getDisabled();
		if(Boolean.TRUE.equals(disabled)) {
			msg = "Etl信息数据代码【"+ dmpCfgEtlEntity.getProcessCode() +"】被禁用";
			log.warn(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
			return;
		}
		
		dmpResponse.setDmpCfgEtlEntity(dmpCfgEtlEntity);
		List<DmpEtlTaskEntity> befortDmpEtlTaskEntityList = this.createEtlTask(dmpRequest , dmpResponse);
		dmpResponse.setBeforeDmpEtlTaskEntityList(befortDmpEtlTaskEntityList);
		chain.doDmpHandler(dmpRequest, dmpResponse);
		
	}
	
	public abstract List<DmpEtlTaskEntity> createEtlTask(DmpEtlCreateRequest dmpRequest, DmpEtlCreateResponse dmpResponse);
}
