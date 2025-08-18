package com.erp.rpc.plm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.feign.BaseDataFeign;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.dto.ProductPurchaseShowDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 产品明细
 * @date 2024-07-03
 * @author tanmujin
 */
@FeignClient(name = "erp-plm", contextId = "productDetailFeign",configuration = {FeignErrorDecoder.class})
public interface ProductDetailFeign extends BaseDataFeign {

    @PostMapping("feign/productDetail/listByIds")
    List<ProductDetailEntity> listByIds(@RequestBody List<String> ids);

    @GetMapping("feign/productDetail/getProductInfoBySkuId")
    ProductDetailDTO.ServiceToWavePickingDTO getProductInfoBySkuId(@RequestParam String skuId);
}
