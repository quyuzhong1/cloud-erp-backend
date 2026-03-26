package com.erp.server.auth.controller.openapi;

import org.springframework.web.bind.annotation.RequestBody;

import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignInvoke;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.auth.config.OpenApi;


/**
 * openapi，公共方法调用
 * @author Administrator
 *
 */
@OpenApi
public class CommonOpenApi {

    /**
     * 
     * @param dto
     * @return
     */
    @OpenApi("common")
    public ApiResult<?> common(@RequestBody FeignInvoke feignInvoke){
        return FeignBuilder.create(null).invokeFeign(feignInvoke);
    }

}
