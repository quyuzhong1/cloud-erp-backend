package com.erp.rpc.plm.feign;

import com.erp.model.plm.entity.ProductPackEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 产品包装
 * @date 2024-08-23
 * @author tanmujin
 */
@FeignClient(name = "erp-plm", contextId = "productPack")
@RequestMapping("/feign/productPack")
public interface ProductPackFeign {

    @PostMapping("/listBySkuIds")
    List<ProductPackEntity> listBySkuIds(@RequestBody List<String> skuIds);
}
