package com.common.business.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lambda
 * @Classname FeignErrorDecoder

 * @Date 2023-05-06 10:13
 * @Created by yl
 */

@Slf4j
@Configuration
public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            String message = Util.toString(response.body().asReader());
            log.error("feign远程调用异常，原始异常信息：{}", message);
            JSONObject jsonObject = JSONObject.parseObject(message);
            if (jsonObject.containsKey("trace")) {
                String trace = StrUtils.null2EmptyWithTrim(jsonObject.getString("trace"));
                if (trace.contains("ServiceException")) {
                    String codeStr = CharSequenceUtil.subBetween(trace, "ServiceException(code=", ", msg");
                    Integer code = Integer.valueOf(codeStr);
                    String msg = "";
                    // 有些异常会返回data
                    if (!trace.contains(", data=")) {
                        msg = CharSequenceUtil.subBetween(trace, "msg=", ")");
                    } else {
                        msg = CharSequenceUtil.subBetween(trace, "msg=", ", data");
                    }
                    return  new ServiceException(code, msg);
                }else{
                    return  new ServiceException(ApiError.Default);
                }
            }else{
                return  new ServiceException(ApiError.Default);
            }
        } catch (Exception e) {
            log.error("FeignErrorDecoder 出错了 {}", e);
        }

         return  new ServiceException(ApiError.Default);
    }
}
