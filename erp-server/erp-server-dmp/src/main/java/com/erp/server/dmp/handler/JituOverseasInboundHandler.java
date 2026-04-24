package com.erp.server.dmp.handler;

import cn.hutool.extra.spring.SpringUtil;
import com.common.business.dto.WebhookResult;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 极兔海外仓入库单状态回转接口
 */
@Slf4j
public class JituOverseasInboundHandler implements WebhookHandler{

    private final DmpInputCreateFactory dmpInputCreateFactory = SpringUtil.getBean(DmpInputCreateFactory.class);

    @Override
    public void verify(String data, Map<String, String> headers, String serviceFlag) {
        return;
    }

    @Override
    public WebhookResult process(String data, Map<String, String> headers, String serviceFlag) {
        if(StringUtils.isBlank(data)){
            return WebhookResult.isSuccess();
        }
        log.warn("webhook 获取极兔海外仓入库单数据,{}",data);
//        try {
//            ThirdWarehouseContext.setData(data);
//            DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
//            dmpInputHotfixCreateRequest.setCfgInputId("1937771436038967500");
//            dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
//        }finally {
//            ThirdWarehouseContext.remove();
//        }
        return WebhookResult.isSuccess();
    }
}
