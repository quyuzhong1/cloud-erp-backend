package com.erp.rpc.oms.feign;

import com.common.business.validator.ValidList;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SkuMappingEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "skuMapping")
public interface SkuMappingFeign {

    /**
     * 查询sku映射关系
     * @Author Luo_WG
     * @Date 2023/11/15 15:03
     * @param dataList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.ListSkuDTO>
     **/
    @PostMapping("feign/skuMapping/listShopSysUserAuthByUserIdList")
    List<SkuMappingDTO.ListSkuDTO> listBySkuNoList(@RequestBody List<SkuMappingDTO.ListSkuParamDTO> dataList);

    /**
     * 根据listingId查询sku映射表
     * @Author Luo_WG
     * @Date 2023/11/15 15:03
     * @param ListingIds
     * @return java.util.List<com.erp.model.oms.entity.SkuMappingEntity>
     **/
    @PostMapping("feign/skuMapping/listByListingIds")
    List<SkuMappingEntity> listByListingIds(@RequestBody List<String> ListingIds);

    /**
     * 根据平台sku查询sku映射信息
     * @Author Luo_WG
     * @Date 2023/11/15 15:14
     * @param listingInfoParamDTO
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.SkuDTO>
     **/
    @PostMapping("feign/skuMapping/listByPlatformSkuNoAndPlatform")
    List<SkuMappingDTO.MappingSkuViewDTO> listByPlatformSkuNoAndPlatform(@RequestBody ListingInfoParamDTO listingInfoParamDTO);
}
