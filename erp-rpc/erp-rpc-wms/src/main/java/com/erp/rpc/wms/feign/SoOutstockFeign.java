package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.SoOutstockDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "soOutstock")
public interface SoOutstockFeign {

    /**
     * 根据来源明细id查询出库表
     * @param sourceDetailId sourceDetailId
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/15 15:03
     **/
    @PostMapping("feign/soOutstock/listDetailBySourceDetailId")
    List<SoOutstockDetailEntity> listDetailBySourceDetailId(@RequestBody List<String> sourceDetailId);
}
