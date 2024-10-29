package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 三方仓发货feign
 */
@FeignClient(name = "erp-wms", contextId = "thirdWarehouseDeliveryFeign")
public interface ThirdWarehouseDeliveryFeign {

    /** 
     * @description 新增
     */
    @PostMapping("feign/thirdWarehouseDelivery/add")
    void add(@RequestBody ThirdWarehouseDeliveryEntity entity);

}
