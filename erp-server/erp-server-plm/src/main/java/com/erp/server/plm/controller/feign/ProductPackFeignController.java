package com.erp.server.plm.controller.feign;

import com.erp.model.plm.dto.ProductPackDTO;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.server.plm.service.ProductPackService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 产品包装信息
 * @date 2023/9/20 11:36
 */
@RestController
@RequestMapping("feign/productPack")
public class ProductPackFeignController {

    @Resource
    private ProductPackService productPackService;

    /**
     * 回填产品包装信息
     * @author Will
     * @date: 2023/9/20 11:39
     * @param productPackList
     */
    @PostMapping("/backFillPackaging")
    public void backFillPackaging(@RequestBody List<ProductPackDTO> productPackList) {
        productPackService.backFillPackaging(productPackList);
    }

    @PostMapping("/listBySkuIds")
    List<ProductPackEntity> listBySkuIds(@RequestBody List<String> skuIds){
        return productPackService.listBySkuIdList(skuIds);
    }
}
