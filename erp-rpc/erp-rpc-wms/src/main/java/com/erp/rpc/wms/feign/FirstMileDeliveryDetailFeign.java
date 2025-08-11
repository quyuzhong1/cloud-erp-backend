package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 头程发货单明细Feign
 * @date 2024-08-31
 * @author tanmujin
 */
@FeignClient(name = "erp-wms", contextId = "firstMileDeliveryDetailFeign", path = "/feign/firstMileDeliveryDetail" ,configuration = {FeignErrorDecoder.class})
public interface FirstMileDeliveryDetailFeign {

    /**
     * 根据发货单ID查询明细
     */
    @PostMapping("/listByMainId")
    List<FirstMileDeliveryDetailEntity> listByMainId(@RequestBody List<String> mainIds);
}
