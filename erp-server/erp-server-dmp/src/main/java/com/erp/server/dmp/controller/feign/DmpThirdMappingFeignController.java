package com.erp.server.dmp.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.server.dmp.service.ThirdMappingService;
import org.springframework.web.bind.annotation.*;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import javax.annotation.Resource;

/**
 * 第三方仓库/店铺映射Feign控制器
 * @date 2024-05-27
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/dmp/thirdMapping")
public class DmpThirdMappingFeignController {

    @Resource
    private ThirdMappingService thirdMappingService;

    @GetMapping("/getBySysId")
    public ThirdMappingEntity getBySysId(@RequestParam String sysId) {
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
}
