package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 发货计划rpc
 * @author will
 * @date 2024/10/18 10:22
 */
@FeignClient(name = "erp-wms", contextId = "common" ,configuration = {FeignErrorDecoder.class})
public interface WmsCommonFeign {

    @GetMapping("feign/common/enumDropDown")
    ApiResult<List<Map<String,Object>>> enumSelect(@RequestParam(value = "type")String type);
}
