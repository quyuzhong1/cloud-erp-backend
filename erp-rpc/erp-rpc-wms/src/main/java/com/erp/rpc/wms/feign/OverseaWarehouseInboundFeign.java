package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 海外仓入库单Feign
 * @date 2024-08-31
 * @author tanmujin
 */
@FeignClient(name = "erp-wms", contextId = "overseaWarehouseInbound", path = "/feign/overseasWarehouseInbound")
public interface OverseaWarehouseInboundFeign {

    /**
     * 根据来源ID查询
     */
    @PostMapping("/listBySourceIds")
    List<OverseasWarehouseInboundEntity> listBySourceIds(@RequestBody List<String> sourceIds);
}
