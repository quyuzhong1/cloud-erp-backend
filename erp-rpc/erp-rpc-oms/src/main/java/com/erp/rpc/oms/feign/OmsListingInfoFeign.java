package com.erp.rpc.oms.feign;

import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "listingInfo")
public interface OmsListingInfoFeign {


    /**
     * 根据产品sku查询库存sku
     * @Author Luo_WG
     * @Date 2023/11/2 17:24
     * @param productSkuIdList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.listStockSkuNoByProductSkuNoView>
     **/
    @PostMapping("feign/listing/listStockSkuNoByProductSkuIds")
    List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> listStockSkuNoByProductSkuIds(@RequestBody List<String> productSkuIdList);

    /**
     * sku映射
     * @Author Luo_WG
     * @Date 2023/11/2 11:19
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/listing/skuMapping")
    Boolean skuMapping(@RequestBody @Validated FbaShipmentDTO.SkuMappingParamDTO dto);


    /**
     * 检查和更新FnSku
     **/
    @PostMapping("feign/listing/checkAndUpdateFnsku")
    List<ListingInfoWithSkuMappingDTO> checkAndUpdateFnsku(@RequestBody @Validated ListingInfoParamDTO dto);
}
