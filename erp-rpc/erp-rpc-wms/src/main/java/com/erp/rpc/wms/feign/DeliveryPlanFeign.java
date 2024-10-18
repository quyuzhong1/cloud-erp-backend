package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * 发货计划rpc
 * @author will
 * @date 2024/10/18 10:22
 */
@FeignClient(name = "erp-wms", contextId = "deliveryPlan")
public interface DeliveryPlanFeign {

    @PostMapping("feign/deliveryPlan/addDeliveryPlan")
    void addDeliveryPlan(@RequestBody @Valid WmsDeliveryPlanDTO.AddDTO addDTO);
}
