package com.erp.server.oms.controller.feign;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.validator.ValidList;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ListingAdvanceQueryDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.server.oms.service.SkuMappingService;
import org.springframework.web.bind.annotation.*;

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
    public List<SkuMappingDTO.MappingSkuViewDTO> listByPlatformSkuNoAndPlatform(@RequestBody ListingInfoParamDTO listingInfoParamDTO) {
        return skuMappingService.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
    }


    /**
     * 通过条件查询sku映射信息
     *
     * @author Jim
     * @date 2023/11/2
     */
    @PostMapping("/list")
    public List<ListingInfoWithSkuMappingDTO> listDTOByType(@RequestBody ListingInfoParamDTO dto) {
        return skuMappingService.findListDto(dto);
    }


    /**
     * 通过skuId查询映射关系列表
     *
     * @author Jim
     * @date 2023/11/2
     */
    @PostMapping("/listByErpSkuIdAndType")
    public List<ListingInfoWithSkuMappingDTO> listByErpSkuIdAndType(@RequestBody List<String> erpSkuIdList,@RequestParam(value = "provideCode") String provideCode,@RequestParam(value = "warehouseId") String warehouseId,@RequestParam(value = "shopId") String shopId) {
        return skuMappingService.listByErpSkuIdAndType(erpSkuIdList,provideCode,warehouseId,shopId);
    }

    /**
     * 通过skuId查询映射关系列表
     *
     * @author Jim
     * @date 2023/11/2
     */
    @PostMapping("/advanceQuerySku")
    @WebAdvanceQuery
    public  List<ListingAdvanceQueryDTO> advanceQuerySku(@RequestBody AdvanceQueryContainer advanceQueryContainer){
        return skuMappingService.advanceQuerySku(advanceQueryContainer);
    }

    /**
     * 根据customerId和skuno 关联查询平台sku
     * @author jack
     * @date: 2024-11-07
     * @param skuParamDTO
     */
    @PostMapping("/listSkuBySkuNos")
    public List<SkuMappingDTO.ProductSkuInfoDTO> listSkuBySkuNos(@RequestBody SkuMappingDTO.SkuParamDTO skuParamDTO) {
        return skuMappingService.listSkuBySkuNos(skuParamDTO);
    }
}
