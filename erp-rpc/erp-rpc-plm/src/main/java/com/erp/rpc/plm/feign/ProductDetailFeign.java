package com.erp.rpc.plm.feign;

import com.common.business.feign.BaseDataFeign;
import com.erp.model.plm.entity.ProductDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 产品明细
 * @date 2024-07-03
 * @author tanmujin
 */
@FeignClient(name = "erp-plm", contextId = "productDetail")
public interface ProductDetailFeign extends BaseDataFeign {

    @PostMapping("feign/productDetail/listByIds")
    List<ProductDetailEntity> listByIds(@RequestBody List<String> ids);
}
