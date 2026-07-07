package com.erp.server.dmp.handler;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.WebhookResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.Kuaidi100WebhookResponseDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.rpc.tms.feign.LogisticsFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import cn.hutool.extra.spring.SpringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 快递100订阅回调处理
 *
 * @author jack
 */
@Slf4j
public class Kuaidi100WebhookHandler implements WebhookHandler {

    private static final String PARAM = "param";
    private static final String SIGN = "sign";

    private LogisticsFeign logisticsFeign;

    @Override
    public void verify(String data, Map<String, String> headers, String serviceFlag) {
        Map<String, String> formMap = parseFormBody(data);
        String param = formMap.get(PARAM);
        String sign = formMap.get(SIGN);
        if (StrUtil.isBlank(param) || StrUtil.isBlank(sign)) {
            throw new ServiceException("快递100回调缺少param或sign");
        }
        String expectedSign = DigestUtils.md5Hex(param.getBytes(StandardCharsets.UTF_8)).toUpperCase();
        if (!expectedSign.equals(sign.trim())) {
            throw new ServiceException("快递100回调验签失败");
        }
    }

    @Override
    public WebhookResult<Kuaidi100WebhookResponseDTO> process(String data, Map<String, String> headers, String serviceFlag) {
        Map<String, String> formMap = parseFormBody(data);
        LogisticsTrackDTO.Kuaidi100WebHookDTO dto = buildWebhookDTO(formMap.get(PARAM), formMap.get(SIGN));
        log.info("webhook 获取快递100订阅回调数据,{}", JSON.toJSONString(dto));
        getLogisticsFeign().webhookByKuaidi100(dto);
        WebhookResult<Kuaidi100WebhookResponseDTO> result = new WebhookResult<>();
        result.setData(Kuaidi100WebhookResponseDTO.success());
        return result;
    }

    private LogisticsFeign getLogisticsFeign() {
        if (logisticsFeign == null) {
            logisticsFeign = SpringUtil.getBean(LogisticsFeign.class);
        }
        return logisticsFeign;
    }

    private LogisticsTrackDTO.Kuaidi100WebHookDTO buildWebhookDTO(String param, String sign) {
        if (StrUtil.isBlank(param)) {
            throw new ServiceException("快递100回调param为空");
        }
        JSONObject paramJson = JSON.parseObject(param);
        JSONObject lastResultJson = paramJson.getJSONObject("lastResult");
        if (lastResultJson == null) {
            throw new ServiceException("快递100回调缺少lastResult");
        }
        LogisticsTrackDTO.Kuaidi100WebHookDTO dto = new LogisticsTrackDTO.Kuaidi100WebHookDTO();
        dto.setRawParam(param);
        dto.setSign(sign);
        dto.setStatus(paramJson.getString("status"));
        dto.setBillstatus(paramJson.getString("billstatus"));
        dto.setMessage(paramJson.getString("message"));
        dto.setLastResult(lastResultJson.toJavaObject(LogisticsTrackDTO.Kuaidi100LastResultDTO.class));
        return dto;
    }

    private Map<String, String> parseFormBody(String data) {
        Map<String, String> formMap = new HashMap<>();
        if (StrUtil.isBlank(data)) {
            return formMap;
        }
        String[] pairs = data.split("&");
        for (String pair : pairs) {
            int index = pair.indexOf("=");
            String key = index < 0 ? pair : pair.substring(0, index);
            String value = index < 0 ? "" : pair.substring(index + 1);
            formMap.put(urlDecode(key), urlDecode(value));
        }
        return formMap;
    }

    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("快递100回调参数URL解码失败", e);
        }
    }
}
