package com.erp.rpc.wms.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 装箱任务相关fegin
 */
@FeignClient(name = "erp-wms", contextId = "packingTaskFeign")
public interface PackingTaskFeign {

    /**
     * B2B称重
     * @param dto
     * @return
     * **/
    @PostMapping("/feign/packingTask/dimensionalWeight")
    ApiResult<String> dimensionalWeight(@RequestBody DimensionalWeightDTO dto);

    /**
     * 根据任务ID批量查询装箱信息
     * @param taskIds 任务ID集合
     */
    @PostMapping("/feign/packingTask/listCartonSpecByTaskIds")
    List<WmsCartonSpecDTO.WmsCartonSpecView> listCartonSpecByTaskIds(@RequestBody List<String> taskIds);
}
