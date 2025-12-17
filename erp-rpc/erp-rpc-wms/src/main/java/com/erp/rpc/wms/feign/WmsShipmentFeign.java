package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.lingxing.FbaReceiveGroupEntity;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-wms", contextId = "shipmentFeign",configuration = {FeignErrorDecoder.class})
public interface WmsShipmentFeign {


    /**
     * 直接消费货件处理
     * @author Jim
     */
    @PostMapping("/feign/shipment/consumer")
    ApiResult<T> consumerPullShipment(@RequestBody PlatformFbaShipmentDTO platformFbaShipmentDTO);


    /**
     * 保存签收记录并检查调拨
     * @author Jim
     */
    @PostMapping("/feign/shipment/saveAndCheckTransfer")
    Boolean saveAndCheckTransfer(@RequestBody FbaReceiveGroupEntity groupEntity);
}
