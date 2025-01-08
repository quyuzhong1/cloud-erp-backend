package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.server.tms.service.LogisticsBillCostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@RestController
@LogSystemModule("物流费用单feign接口")
@RequestMapping("/feign/logisticsBillCost")
public class LogisticsBillCostFeignController {

    @Resource
    private LogisticsBillCostService logisticsBillCostService;


    /**
     * @description: 更新店铺负责人
     * @author Will
     * @date: 2024/5/11 18:33
     * @param dto
     * @return Boolean
     */
    @PostMapping("/updateShopCharge")
    public Boolean updateShopCharge(@RequestBody LogisticsBillCostDTO.UpdateShopChargeDTO dto) {
        Boolean flag = logisticsBillCostService.updateShopCharge(dto);
        return flag;
    }

    @PostMapping("/getActualLogisticCost")
    public BigDecimal getActualLogisticCost(@RequestBody String soId) {
        return logisticsBillCostService.getActualLogisticCost(soId);
    }
    
    @PostMapping("/generateLogisticsBill")
    public void generateLogisticsBill(@RequestBody SoReturnInstockEntity entity) {
    	logisticsBillCostService.generateLogisticsBill(entity);
    }
}
