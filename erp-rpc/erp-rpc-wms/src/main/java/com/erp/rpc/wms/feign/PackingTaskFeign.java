package com.erp.rpc.wms.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 装箱任务相关fegin
 */
@FeignClient(name = "erp-wms", contextId = "packingTaskFeign")
public interface PackingTaskFeign {

    /**
     * B2B称重
     * @param dto
     * @return
     */
    @PostMapping("/feign/packingTask/dimensionalWeightTob")
    ApiResult<String> dimensionalWeightTob(@RequestBody DimensionalWeightDTO dto);

    @PostMapping("/feign/packingTask/dimensionalWeightFba")
    ApiResult<String> dimensionalWeightFba(@RequestBody DimensionalWeightDTO dto);

    @PostMapping("/feign/packingTask/dimensionalWeightThird")
    ApiResult<String> dimensionalWeightThird(@RequestBody DimensionalWeightDTO dto);
}
