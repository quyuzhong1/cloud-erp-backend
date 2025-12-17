package com.erp.rpc.plm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.plm.entity.ProductPackEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 产品包装
 * @date 2024-08-23
 * @author tanmujin
 */
@FeignClient(name = "erp-plm", contextId = "productPackFeign",configuration = {FeignErrorDecoder.class})
public interface ProductPackFeign {

    @PostMapping("/feign/productPack/listBySkuIds")
    List<ProductPackEntity> listBySkuIds(@RequestBody List<String> skuIds);

    @PostMapping("/feign/productPack/listSingleBySkuIds")
    Map<String, BigDecimal> listSingleBySkuIds(@RequestBody List<String> skuIds);
}
