package com.erp.server.dmp.inout.handler.input.task.init.api.spt;

import com.common.business.enums.OmsPlatformEnum;
import com.erp.server.dmp.inout.handler.input.task.init.api.eccang.EccangInboundInitHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * dmp输入init任务基础处理器下的速派通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class SptInboundInitHandler extends EccangInboundInitHandler {

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_SPT;
    }

}
