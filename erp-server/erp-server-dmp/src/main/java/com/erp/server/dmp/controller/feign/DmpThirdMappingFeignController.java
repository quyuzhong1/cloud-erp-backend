package com.erp.server.dmp.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.server.dmp.service.ThirdMappingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.erp.model.dmp.dto.ThirdMappingDTO;

import javax.annotation.Resource;

/**
 * 第三方仓库/店铺映射Feign控制器
 *
 * @author tanmujin
 * @date 2024-05-27
 */
@RestController
@RequestMapping("/feign/dmp/thirdMapping")
public class DmpThirdMappingFeignController {

    @Resource
    private ThirdMappingService thirdMappingService;

    @GetMapping("/getBySysId")
    public ThirdWarehouseEntity getBySysId(@RequestParam String sysId) {
        return thirdMappingService.getBySysId(sysId);
    }

    @PostMapping("/getByThirdId")
    public Boolean getByThirdId(@RequestBody ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        return thirdMappingService.getByThirdId(viewParamDTO);
    }

    @PostMapping("/batchAdd")
    public BaseResultDTO.AddDTO batchAdd(@RequestBody ThirdMappingDTO.FeignMappingDTO feignMappingDTO) {
        return thirdMappingService.batchAdd(feignMappingDTO);
    }

    @PostMapping("/add")
    public BaseResultDTO.AddDTO add(@RequestBody @Validated ThirdMappingDTO.AddDTO dto) {
        return thirdMappingService.add(dto);
    }
}
