package com.erp.server.dmp.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.security.HmacSHA256Utils;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.rpc.tms.feign.LogisticsFeign;

import java.util.Map;

/**
 * 订单出口回告处理类
 */
public class OrderOutboundHandler implements WebhookHandler{

    private final LogisticsFeign logisticsFeign = SpringUtil.getBean(LogisticsFeign.class);
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
        LogisticsTrackDTO.TrackWebHookDTO trackWebHookDTO = JSONUtil.toBean(data, LogisticsTrackDTO.TrackWebHookDTO.class);
        logisticsFeign.webhookByTrack123(trackWebHookDTO);
        return null;
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
