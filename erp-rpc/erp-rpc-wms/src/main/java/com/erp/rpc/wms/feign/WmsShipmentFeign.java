package com.erp.rpc.wms.feign;

import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "shipmentFeign")
public interface WmsShipmentFeign {


    /**
     * 直接消费货件处理
     * @author Jim
     */
    @PostMapping("/feign/shipment/consumer")
    ApiResult<?> consumerPullShipment(@RequestBody PlatformFbaShipmentDTO platformFbaShipmentDTO);


    /**
     * 保存签收记录并检查调拨
     * @author Jim
     */
    @PostMapping("/feign/shipment/saveAndCheckTransfer")
    Boolean saveAndCheckTransfer(@RequestBody List<FbaShipmentReceiveEntity> entityList);
}
