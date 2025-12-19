package com.erp.sdk.oms.amz.spapi.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * 亚马逊异常工具类
 *
 * @author Jim
 * @date 2024/01/31
 */
@Slf4j
public class AmazonSpApiExceptionUtils {

    /**
     * 检查是否授权异常
     */
    public static boolean isUnauthorized(Exception exception) {
        if (!(exception instanceof ServiceException) && !(exception instanceof ApiException)) {
            return false;
        }
        // 系统内部定义授权异常
        if (exception instanceof ServiceException) {
            ServiceException serviceException = (ServiceException) exception;
            if (ApiError.SHOP_FBA_MARKETPLACE_DISABLED.getCode().equals(serviceException.getCode())) {
                return true;
            }
        }
        // 亚马逊定义授权异常
        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            if (403 != apiException.getCode()) {
                return false;
            }
            String responseBody = apiException.getResponseBody();
            if (StringUtils.isBlank(responseBody)) {
                return false;
            }
            // {"errors": [{
            //      "code": "Unauthorized",
            //      "message": "Access to requested resource is denied.",
            //      "details": ""
            //    }]}
            JSONArray jsonArray = new JSONObject(responseBody).getJSONArray("errors");
            if (CollectionUtils.isEmpty(jsonArray)){
                return false;
            }
            String code = new JSONObject(jsonArray.get(0)).getStr("code");
            if (StringUtils.isBlank(code)) {
                return false;
            }
            return "Unauthorized".equalsIgnoreCase(code);
        }
        return false;
    }

}
