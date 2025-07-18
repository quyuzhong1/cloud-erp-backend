package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.erp.server.tms.service.LogisticsBillCostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/import")
public class ImportTmsFeignController {
    @Resource
    private LogisticsBillCostService logisticsBillCostService;


    @PostMapping("/logisticsBillCost")
    public BaseDTO.ImportResultDTO importLogisticsBillCost(@RequestBody BaseDTO.ImportDTO dto) {
        return logisticsBillCostService.importLogisticsBillCost(dto);
    }
}
