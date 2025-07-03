package com.erp.server.dmp.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.wms.aliexpress.model.order.AliexpressOrderConfirmDTO;
import io.seata.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 订单出口回告处理类
 */
public class OrderOutboundHandler implements WebhookHandler{

    private static final Logger log = LoggerFactory.getLogger(OrderOutboundHandler.class);

    private final DmpInputCreateFactory dmpInputCreateFactory = SpringUtil.getBean(DmpInputCreateFactory.class);

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
    public String process(String data, Map<String, String> headers, String serviceFlag) {
        if(StringUtils.isBlank(data)){
            return "";
        }
        log.warn("webhook 获取出库单数据,{}",data);
        try {
            ThirdWarehouseContext.setData(data);
            DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
            dmpInputHotfixCreateRequest.setCfgInputId("1937771436038967500");
            dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
        }finally {
            ThirdWarehouseContext.remove();
        }
        return "success";
    }

//    private boolean verifySignature(String data, String signature) {
//        String generatedSignature = HmacSHA256Utils.hmacSHA256(data, SECRET_KEY);
//        // 注意：这里假设传入的签名是"sha256="前缀后的实际Base64编码值
//        if (signature.startsWith("sha256=")) {
//            signature = signature.substring(7);
//        }
//        return signature.equals(generatedSignature);
//    }
}
