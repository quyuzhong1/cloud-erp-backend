package com.erp.server.dmp.handler;

import cn.hutool.Hutool;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.common.core.security.HmacSHA256Utils;
import com.erp.model.dmp.track123.WebhookRequest;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

/**
 * @author zdy
 * @ClassName Track123WebhookHandler
 * @description: TODO
 * @date 2024年11月11日
 * @version: 1.0
 */
public class Track123WebhookHandler implements WebhookHandler{
    // 预先约定的Secret
    private static final String SECRET_KEY = "9fa500686633410a84ff0b00daed555e";
    // 允许的时间偏差（秒）
    private static final long MAX_AGE = 5 * 60; // 5 minutes

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

    private boolean verifySignature(String data, String signature) {
        String generatedSignature = HmacSHA256Utils.hmacSHA256(data, SECRET_KEY);
        // 注意：这里假设传入的签名是"sha256="前缀后的实际Base64编码值
        if (signature.startsWith("sha256=")) {
            signature = signature.substring(7);
        }
        return signature.equals(generatedSignature);
    }
}
