package com.erp.server.wms.controller.feign;

import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.server.wms.service.PurchaseReturnOrderDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/14 11:47
 */
@RestController
@RequestMapping("feign/purchaseReturnOrder")
public class PurchaseReturnOrderFeignController {

    @Resource
    private PurchaseReturnOrderDetailService purchaseReturnOrderDetailService;

    /**
     * @description: 根据采购订单明细ids查询退货明细
     * @author Will
     * @date: 2023/4/14 11:55
     * @param SourceDetailIds
     * @return List<PurchaseReturnOrderDetailEntity>
     */
    @PostMapping("/listDetailBySourceDetailIds")
    public List<PurchaseReturnOrderDetailEntity> listBySourceDetailIds(@RequestBody List<String> SourceDetailIds) {
        return purchaseReturnOrderDetailService.listBySourceDetailIds(SourceDetailIds);
    }
}
