package com.erp.server.scm.controller.feign;


import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.scm.service.PurchaseOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购订单feign
 * @Author Luo_WG
 * @Date 2023/4/13 11:11
 **/
@RestController
@RequestMapping("feign/purchaseOrder")
public class PurchaseOrderFeignController {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    /**
     * 根据id查询采购订单
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @GetMapping("/getPurchaseOrderById")
    public PurchaseOrderEntity getPurchaseOrderById(@RequestParam("id") String id) {
        return purchaseOrderService.getById(id);
    }
}
