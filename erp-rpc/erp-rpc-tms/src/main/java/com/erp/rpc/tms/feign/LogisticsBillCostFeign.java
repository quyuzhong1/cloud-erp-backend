package com.erp.rpc.tms.feign;

import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.wms.entity.SoReturnInstockEntity;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "erp-tms", contextId = "logisticsBillCost")
public interface LogisticsBillCostFeign {

    /**
     * @description: 更新店铺负责人
     * @author Will
     * @date: 2024/5/11 18:22
     * @param dto
     * @return Boolean
     */
    @PostMapping("/feign/logisticsBillCost/updateShopCharge")
    Boolean updateShopCharge(@RequestBody LogisticsBillCostDTO.UpdateShopChargeDTO dto);

    /**
     * @description: 查询订单实际物流运费
     */
    @PostMapping("/feign/logisticsBillCost/getActualLogisticCost")
    BigDecimal getActualLogisticCost(@RequestBody String soId);
    
    
    @PostMapping("/feign/logisticsBillCost/generateLogisticsBill")
    void generateLogisticsBill(@RequestBody SoReturnInstockEntity entity);
}
