package com.erp.server.wms.controller.feign;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.server.wms.service.PackingTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@LogSystemModule("装箱任务")
@RequestMapping("/feign/packingTask")
public class PackingTaskFeignController extends BaseController {

    @Resource
    private PackingTaskService packingTaskService;


    /**
     * 设备扫描称重
     */
    @PostMapping("/dimensionalWeight")
    public ApiResult<String> dimensionalWeight(@RequestBody @Validated DimensionalWeightDTO dto) {
        return packingTaskService.dimensionalWeight(dto);
    }

}
