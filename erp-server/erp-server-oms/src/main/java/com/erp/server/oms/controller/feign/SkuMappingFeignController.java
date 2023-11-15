package com.erp.server.oms.controller.feign;

import com.common.business.validator.ValidList;
import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.server.oms.service.SkuMappingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/skuMapping")
public class SkuMappingFeignController extends BaseController {
    @Resource
    private SkuMappingService skuMappingService;

    /**
     * 查询sku映射关系
     * @Author Luo_WG
     * @Date 2023/11/15 15:04
     * @param dataList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.ListSkuDTO>
     **/
    @PostMapping("/listShopSysUserAuthByUserIdList")
    public List<SkuMappingDTO.ListSkuDTO> listBySkuNoList(@RequestBody List<SkuMappingDTO.ListSkuParamDTO> dataList) {
        return skuMappingService.listBySkuNoList(dataList);
    }

    /**
     * 根据listingId查询sku映射表
     * @Author Luo_WG
     * @Date 2023/11/15 15:04
     * @param ListingIds
     * @return java.util.List<com.erp.model.oms.entity.SkuMappingEntity>
     **/
    @PostMapping("/listByListingIds")
    public List<SkuMappingEntity> listByListingIds(@RequestBody List<String> ListingIds) {
        return skuMappingService.listByListingIds(ListingIds);
    }

    /**
     * 根据平台sku查询sku映射信息
     * @Author Luo_WG
     * @Date 2023/11/15 15:14
     * @param listingInfoParamDTO
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.SkuDTO>
     **/
    @PostMapping("/listByPlatformSkuNoAndPlatform")
    public List<SkuMappingDTO.SkuDTO> listByPlatformSkuNoAndPlatform(@RequestBody ListingInfoParamDTO listingInfoParamDTO) {
        return skuMappingService.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
    }
}
