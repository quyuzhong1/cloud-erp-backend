package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ListingAdvanceQueryDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SkuMappingEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "skuMappingFeign",configuration = {FeignErrorDecoder.class})
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

    /**
     * 通过条件查询ListingInfoEntity列表
     *
     * @author Jim
     * @date 2023/11/2
     */
    @PostMapping("feign/skuMapping/list")
    List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingList(@RequestBody ListingInfoParamDTO dto);


    /**
     * 通过skuId查询映射关系列表
     *
     * @author Jim
     * @date 2023/11/2
     */
    @PostMapping("feign/skuMapping/listByErpSkuIdAndType")
    List<ListingInfoWithSkuMappingDTO> listByErpSkuIdAndType(@RequestBody List<String> erpSkuIdList,@RequestParam(value = "provideCode") String provideCode,@RequestParam(value = "warehouseId") String warehouseId,@RequestParam(value = "shopId") String shopId);

    /**
     * 高级查询
     * @return erp skuId
     */
    @PostMapping("feign/skuMapping/advanceQuerySku")
    List<ListingAdvanceQueryDTO> advanceQuerySku(@RequestBody AdvanceQueryContainer advanceQueryContainer);
    /**
     * 根据customerId和skuno 关联查询平台sku
     * @author jack
     * @date: 2024-11-07
     * @param skuParamDTO
     */
    @PostMapping("feign/skuMapping/listSkuBySkuNos")
    List<SkuMappingDTO.ProductSkuInfoDTO> listSkuBySkuNos(@RequestBody SkuMappingDTO.SkuParamDTO skuParamDTO);
}
