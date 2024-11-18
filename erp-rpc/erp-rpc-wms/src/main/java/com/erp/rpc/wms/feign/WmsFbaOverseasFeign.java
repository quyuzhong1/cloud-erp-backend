package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "fbaOverseasFeign")
public interface WmsFbaOverseasFeign {

    /**
     * 批量保存FBA库存信息和预留明细
     *
     * @Author Jim
     * @Date 2023-11-08
     **/
    @PostMapping("/feign/fbaOverseas/listWarehouseByIds")
    List<OverseasProviderWarehouseEntity> listWarehouseByIds(@RequestBody List<String> warehouseByIds);

}
