package com.erp.server.scm.controller.feign;

import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import com.erp.server.scm.service.SalesDemandService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购价目表
 * @Author Luo_WG
 * @Date 2023/6/30 19:44
 **/
@RestController
@RequestMapping("feign/purchasePrice")
public class PurchasePriceFeignController {

    @Resource
    private PurchasePriceService purchasePriceService;

    /**
     * 根据供应商Ids查询最新的sku价格信息
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/listSupplierSkuPrice")
    public List<PurchasePriceDTO.SupplierSkuPrice> listSupplierSkuPrice(@RequestBody List<String> ids) {
        return purchasePriceService.listSupplierSkuPrice(ids);
    }
}
