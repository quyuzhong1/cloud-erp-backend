package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.scm.entity.SalesDemandEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 备货申请单Feign
 * @date 2025-03-24
 * @author jack
 */
@FeignClient(name = "erp-scm", contextId = "salesDemand",configuration = {FeignErrorDecoder.class})
public interface SaleDemandFeign {
    /**
     *
     */
    @PostMapping("/feign/salesDemand/listBySourceIds")
    List<SalesDemandEntity> listBySourceIds(List<String> sourceIds);

}
