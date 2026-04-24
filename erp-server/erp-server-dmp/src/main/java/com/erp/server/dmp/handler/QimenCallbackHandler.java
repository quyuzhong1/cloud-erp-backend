package com.erp.server.dmp.handler;

import cn.hutool.extra.spring.SpringUtil;
import com.common.business.dto.WebhookResult;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import io.seata.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 订单出口回告处理类
 */
public class QimenCallbackHandler implements WebhookHandler{

    private static final Logger log = LoggerFactory.getLogger(QimenCallbackHandler.class);

//    private final DmpInputCreateFactory dmpInputCreateFactory = SpringUtil.getBean(DmpInputCreateFactory.class);

    @Override
    public void verify(String data, Map<String, String> headers, String serviceFlag) {
        return;
//        LogisticsTrackDTO.TrackWebHookDTO trackWebHookDTO = JSONUtil.toBean(data, LogisticsTrackDTO.TrackWebHookDTO.class);
//        LogisticsTrackDTO.Verify verify = trackWebHookDTO.getVerify();
//        String timestamp = verify.getTimestamp();
//        String signature = verify.getSignature();
//        // 校验时间戳
//        if (CharSequenceUtil.isNotBlank(timestamp) && Math.abs((System.currentTimeMillis() - Long.parseLong(timestamp))/1000) > MAX_AGE) {
//            throw new ServiceException("Request is too old or timestamp is missing");
//        }
//
//        // 校验签名
//        if (CharSequenceUtil.isNotBlank(signature) && !verifySignature(JSONUtil.toJsonStr(trackWebHookDTO.getData()), signature)) {
//            throw new ServiceException("Invalid signature");
//        }
    }

    @Override
    public WebhookResult process(String data, Map<String, String> headers, String serviceFlag) {
        if(StringUtils.isBlank(data)){
            return WebhookResult.isSuccess();
        }
        log.warn("webhook 获取奇门数据,{}",data);
//        try {
//            ThirdWarehouseContext.setData(data);
//            DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
//            dmpInputHotfixCreateRequest.setCfgInputId("1938157629872296175");
//            dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
//        }finally {
//            ThirdWarehouseContext.remove();
//        }
        return WebhookResult.isSuccess();
    }
}
