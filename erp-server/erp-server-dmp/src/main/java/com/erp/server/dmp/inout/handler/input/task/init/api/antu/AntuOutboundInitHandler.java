package com.erp.server.dmp.inout.handler.input.task.init.api.antu;

import com.common.business.enums.OmsPlatformEnum;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.server.dmp.inout.handler.input.task.init.api.eccang.EccangOutboundInitHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * dmp输入init任务基础处理器下的安兔api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class AntuOutboundInitHandler extends EccangOutboundInitHandler {
	@Override
	public OmsPlatformEnum getPlatForm() {
		return OmsPlatformEnum.OMS_ANTU;
	}

	@Override
	public DmpBasicSystemCodeEnum getDmpBasicSystemCodeEnum() {
		return DmpBasicSystemCodeEnum.ANTU;
	}
}
