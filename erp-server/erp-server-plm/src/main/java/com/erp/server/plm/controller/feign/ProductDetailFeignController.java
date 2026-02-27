package com.erp.server.plm.controller.feign;

import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductPurchaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 产品明细
 * @date 2024-07-03
 * @author tanmujin
 */
@RestController
@RequestMapping("feign/productDetail")
public class ProductDetailFeignController {

    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private ProductPurchaseService productPurchaseService;

    @PostMapping("/listByIds")
    public List<ProductDetailEntity> listByIds(@RequestBody List<String> ids){
        return productDetailService.listByIds(ids);
    }

    @GetMapping("/getProductInfoBySkuId")
    ProductDetailDTO.ServiceToWavePickingDTO getProductInfoBySkuId(@RequestParam String skuId){
        return productPurchaseService.getProductInfoBySkuId(skuId);
    }

    @PostMapping("/getSkuBySyncKingdeeId")
    ProductDetailEntity getSkuBySyncKingdeeId(@RequestBody String syncKingdeeId){
        return productDetailService.getSkuBySyncKingdeeId(syncKingdeeId);
    }
}
