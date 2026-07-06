package com.erp.server.plm.controller.feign;

import com.common.core.anno.LogAction;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.SkuStdCostDTO;
import com.erp.server.plm.service.SkuStdCostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/feign/skuStdCost")
public class SkuStdCostFeignController {

    @Resource
    private SkuStdCostService skuStdCostService;


    @PostMapping("/updateSkuStdCost")
    @LogAction(value = LogActionEnum.UPDATE, desc = "SKU标准成本更新")
    public void updateSkuStdCost(@RequestBody SkuStdCostDTO.UpdateDTO dto) {
        skuStdCostService.update(dto);
    }


}
