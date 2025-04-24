package com.erp.server.dmp.inout.handler.input.task.init.api.mabang;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.ParamHeaderVO;
import com.common.core.security.HmacSHA256Utils;

import java.util.HashMap;
import java.util.Map;

public class MabangTool {
    private static Integer APP_KEY = 200780;

    private static String MANGBANG_KEY = "13c324fa18feaaeb0ebcc8a7746ebfca";

    public static ParamHeaderVO getParamMap(String method, Integer pageIndex, Map<String,Object> params) {
        params.put("page", pageIndex);
        Map<String, Object> paramMap = new HashMap(16);
        paramMap.put("api", method);
        paramMap.put("appkey", APP_KEY);
        paramMap.put("version", 1);
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("data",params);
        String paramStr = JSONUtil.toJsonStr(paramMap);
        String sign = HmacSHA256Utils.hmacSHA256(paramStr, MANGBANG_KEY);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", sign);
        return new ParamHeaderVO(paramStr, headerMap);
    }
}
