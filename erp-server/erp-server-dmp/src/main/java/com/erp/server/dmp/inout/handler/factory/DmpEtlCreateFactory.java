package com.erp.server.dmp.inout.handler.factory;

import com.erp.server.dmp.inout.dto.request.DmpEtlHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.etl.create.DmpEtlHotfixCreateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.server.dmp.inout.dto.request.DmpEtlCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpEtlCreateResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.etl.create.DmpEtlCreateHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Etl任务创建工厂，添加handler给handler链路执行
 * @author Administrator
 *
 */
@Component
@Slf4j
public class DmpEtlCreateFactory{
	
	@Autowired
	private DmpEtlCreateHandler dmpEtlCreateHandler;
    @Autowired
    private DmpEtlHotfixCreateHandler dmpEtlHotfixCreateHandler;
	
	/**
	 * 创建正常任务
	 * @param dmpEtlCreateRequest
	 */
	public void createEtlTask(DmpEtlCreateRequest dmpEtlCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpEtlCreateHandler);
		bean.doDmpHandler(dmpEtlCreateRequest, new DmpEtlCreateResponse());
	}

    /**
     * 创建热点任务
     * @param dmpRequest
     */
    public void createHotfixEtlTask(DmpEtlHotfixCreateRequest dmpRequest) {
        DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
        bean.addDmpHandler(dmpEtlHotfixCreateHandler);
        bean.doDmpHandler(dmpRequest, new DmpEtlCreateResponse());
    }
}
