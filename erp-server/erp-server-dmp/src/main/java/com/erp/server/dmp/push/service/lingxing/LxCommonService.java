package com.erp.server.dmp.push.service.lingxing;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LxCommonService {

    public static final String REQUEST_URL = "requestUrl";

    public static final String REQUEST_DATA = "requestData";

    public static final String REQUEST_LX = "requestLx";


    /**
     * 公共请求领星
     */
    public ApiResult<?> requestLx(Object ext) {
        JSONObject parseObject = JSON.parseObject(ext.toString());
        String apiUri = parseObject.getString(REQUEST_URL);
        JSONObject requestData = parseObject.getJSONObject(REQUEST_DATA);
        if (null == requestData || StringUtils.isBlank(apiUri)){
            return ApiResult.error("requestUrl或requestData为空", ext);
        }
        Result<Object> responseData = LingxingApiUtils.commonSync(apiUri, requestData);
        log.warn("请求领星响应报文：{}", responseData);
        if (!"0".equalsIgnoreCase(responseData.getCode())) {
            String errorMsg = StrUtil.format("同步领星失败:path={},request={}, result={}", apiUri, ext, JSONUtil.toJsonStr(responseData));
            log.error(errorMsg);
            return ApiResult.error(errorMsg, responseData);
        }

        JSONObject responseObject = null;
        try {
            responseObject = JSON.parseObject(JSON.toJSONString(responseData.getData()));
            return ApiResult.success(responseObject);
        } catch (Exception e) {
            String msg = CharSequenceUtil.format("解析领星响应json失败:{}", ExceptionUtil.stacktraceToString(e));
            return ApiResult.error(msg, responseObject);
        }
    }
}