package com.erp.server.dmp.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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
public class JituOutboundHandler implements WebhookHandler{

    private final DmpInputCreateFactory dmpInputCreateFactory = SpringUtil.getBean(DmpInputCreateFactory.class);

    @Override
    public void verify(String data, Map<String, String> headers, String serviceFlag) {
        return;
    }

    @Override
    public WebhookResult process(String data, Map<String, String> headers, String serviceFlag) {
        if(StringUtils.isBlank(data)){
            return WebhookResult.isJituFailed(-1,"回传数据为空");
        }
        JSONObject jsonObject = JSONUtil.parseObj(data);
        String requestId = jsonObject.getStr("requestId");
        log.warn("webhook 获取极兔出库单数据,{}",data);
        try {
            ThirdWarehouseContext.setData(data);
            DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
            dmpInputHotfixCreateRequest.setCfgInputId("1938157629872288001");
            dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
        }finally {
            ThirdWarehouseContext.remove();
        }
        return WebhookResult.isJituSuccess(requestId,0,"回传成功");
    }
}
