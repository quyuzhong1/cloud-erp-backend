package com.erp.rpc.wms.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * 发货计划rpc
 * @author will
 * @date 2024/10/18 10:22
 */
@FeignClient(name = "erp-wms", contextId = "deliveryPlan")
public interface DeliveryPlanFeign {

    @PostMapping("feign/deliveryPlan/addDeliveryPlan")
    BaseResultDTO.AddDTO addDeliveryPlan(@RequestBody @Valid WmsDeliveryPlanDTO.AddDTO addDTO);

    /**
     * 根据来源id查询
     */
    @PostMapping("feign/deliveryPlan/listBySourceIdList")
    List<WmsDeliveryPlanDetailEntity> listBySourceIdList(@RequestBody List<String> idList);
}
