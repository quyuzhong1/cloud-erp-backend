package com.erp.rpc.oms.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.wms.dto.FbaShipmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "listingInfo")
public interface OmsListingInfoFeign {


    /**
     * 通过条件查询ListingInfoEntity列表
     *
     * @author Jim
     * @date 2023/11/2
     */
    @PostMapping("feign/listing/list")
    List<ListingInfoEntity> list(@RequestBody ListingInfoParamDTO dto);

    /**
     * 根据产品sku查询库存sku
     * @Author Luo_WG
     * @Date 2023/11/2 17:24
     * @param productSkuNoList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.listStockSkuNoByProductSkuNoView>
     **/
    @PostMapping("feign/listing/listStockSkuNoByProductSkuNo")
    List<SkuMappingDTO.listStockSkuNoByProductSkuNoView> listStockSkuNoByProductSkuNo(List<String> productSkuNoList);

    /**
     * sku映射
     * @Author Luo_WG
     * @Date 2023/11/2 11:19
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/listing/skuMapping")
    Boolean skuMapping(@RequestBody @Validated FbaShipmentDTO.skuMappingParamDTO dto);


}
