package com.common.business.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.FeignServiceException;
import com.common.core.utils.StrUtils;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lambda
 * @Classname FeignErrorDecoder
 * @Description TODO
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
            JSONObject jsonObject = JSONObject.parseObject(message);
            if (jsonObject.containsKey("trace")) {
                String trace = StrUtils.null2EmptyWithTrim(jsonObject.getString("trace"));
                if (trace.contains("ServiceException")) {
                    String codeStr = StrUtil.subBetween(trace, "ServiceException(code=", ", msg");
                    Integer code = Integer.valueOf(codeStr);
                    String msg = StrUtil.subBetween(trace, "msg=", ")");
                    return  new FeignServiceException(code, msg);
                }else{
                    return  new FeignServiceException(ApiError.Default);
                }
            }else{
                return  new FeignServiceException(ApiError.Default);
            }
        } catch (Exception e) {
            log.error("FeignErrorDecoder 出错了 {}", e);
        }

        return null;
    }
}
