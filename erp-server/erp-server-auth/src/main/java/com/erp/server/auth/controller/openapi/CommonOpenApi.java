package com.erp.server.auth.controller.openapi;

import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.wms.feign.WmsFeign;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignInvoke;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.auth.config.OpenApi;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;


/**
 * openapi，公共方法调用
 * @author Administrator
 *
 */
@OpenApi
public class CommonOpenApi {
    @Resource
    private WmsFeign wmsFeign;
    @Resource
    private FileFeign fileFeign;
    /**
     * 
     * @param feignInvoke
     * @return
     */
    @OpenApi("common")
    public ApiResult<?> common(@RequestBody FeignInvoke feignInvoke){
        return FeignBuilder.create(null).invokeFeign(feignInvoke);
    }

    /**
     * WMS字典列表
     * @param key
     * @return
     */
    @OpenApi("wmsDictList")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> wmsDictList(String key) {
        return wmsFeign.dictList(key);
    }
}
