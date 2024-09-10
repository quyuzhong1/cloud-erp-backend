package com.erp.rpc.wms.feign;

import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 采购申请单明细Feign
 * @date 2024-09-10
 * @author tanmujin
 */
@FeignClient(name = "erp-scm", contextId = "purchaseApplicationDetailFeign")
public interface PurchaseApplicationDetailFeign {

    /**
     * 根据采购申请单ID查询明细
     * @param mainIds 单据ID
     * @return 采购申请单明细实体
     * @date: 2024-09-10
     * @author: tanmujin
     */
    @PostMapping("/feign/purchaseApplicationDetail/listByMainIds")
    List<PurchaseApplicationDetailEntity> listByMainIds(@RequestBody List<String> mainIds);
}
