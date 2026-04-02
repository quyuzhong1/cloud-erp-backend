package com.sdk.tms.kuaidi100.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.OkHttpUtils;
import com.sdk.tms.kuaidi100.model.request.Kuaidi100QueryParam;
import com.sdk.tms.kuaidi100.model.response.Kuaidi100QueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：快递100实时查询服务类
 *
 * @author jack
 * @date 2026-03-31
 */
@Slf4j
@Service
public class Kuaidi100Service {

    private static final String QUERY_URL = "https://poll.kuaidi100.com/poll/query.do";

    /**
     * 实时查询快递轨迹
     *
     * @param customer 授权码
     * @param key      授权密钥
     * @param param    查询参数
     * @return 查询结果
     * @author jack
     * @date 2026-03-31
     */
    public Kuaidi100QueryResponse getTrack(String customer, String key, Kuaidi100QueryParam param) {
        try {
            String paramJson = JSONObject.toJSONString(param);
            // 签名规则：MD5(param + key + customer).toUpperCase()
            String sign = DigestUtil.md5Hex(paramJson + key + customer).toUpperCase();

            Map<String, Object> formParams = new HashMap<>();
            formParams.put("customer", customer);
            formParams.put("sign", sign);
            formParams.put("param", paramJson);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");

            log.info("快递100实时查询请求参数：{}", formParams);
            String response = OkHttpUtils.doPost(QUERY_URL, formParams, headers);
            log.info("快递100实时查询响应结果：{}", response);

            return JSONObject.parseObject(response, Kuaidi100QueryResponse.class);
        } catch (Exception e) {
            log.error("快递100实时查询异常", e);
            return null;
        }
    }
}
