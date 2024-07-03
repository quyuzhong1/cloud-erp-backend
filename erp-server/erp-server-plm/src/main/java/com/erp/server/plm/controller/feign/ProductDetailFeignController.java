package com.erp.server.plm.controller.feign;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.service.ProductDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/listByIds")
    public List<ProductDetailEntity> listByIds(@RequestBody List<String> ids){
        return productDetailService.listByIds(ids);
    }
}
