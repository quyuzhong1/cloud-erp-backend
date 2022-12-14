package com.erp.server.plm.controller.feign;

import com.erp.model.plm.dto.CleanSkuDto;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.service.ProductDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 查询sku
 * @Author Luo_WG
 * @Date 2022/12/14 15:09
 **/
@RestController
@RequestMapping("plm/feign/product")
public class ProductSkuFeignController {

    @Resource
    private ProductDetailService productDetailService;

    /**
     * 根据sku查询sku表信息
     * @Author Luo_WG
     * @Date 2022/12/14 15:24
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    @PostMapping("/getProductIdBySku")
    public CleanSkuDto getProductIdBySku(@RequestBody String sku) {
        return productDetailService.getProductIdBySkuClean(sku);
    }
}
