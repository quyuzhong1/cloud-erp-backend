package com.erp.server.dmp.inout.handler.input.task.init.api.antu;

import com.common.business.enums.OmsPlatformEnum;
import com.erp.server.dmp.inout.handler.input.task.init.api.eccang.EccangInboundInitHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * dmp输入init任务基础处理器下的安兔api获取数据方式
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class AntuInboundInitHandler extends EccangInboundInitHandler {

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_ANTU;
    }

}
