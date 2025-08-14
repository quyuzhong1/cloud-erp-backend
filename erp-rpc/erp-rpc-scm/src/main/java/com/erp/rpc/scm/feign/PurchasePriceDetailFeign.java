package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 供应商采购价目表Feign
 * @date 2024-09-06
 * @author tanmujin
 */
@FeignClient(name = "erp-scm", contextId = "purchasePriceDetail",configuration = {FeignErrorDecoder.class})
public interface PurchasePriceDetailFeign {

    @PostMapping("/feign/purchasePriceDetail/getTaxPrice")
    List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getTaxPrice(@RequestBody @Validated PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto);

    @PostMapping("/feign/purchasePriceDetail/listTaxPrice")
    List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> listTaxPrice(@RequestBody @Validated List<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> list);
}
