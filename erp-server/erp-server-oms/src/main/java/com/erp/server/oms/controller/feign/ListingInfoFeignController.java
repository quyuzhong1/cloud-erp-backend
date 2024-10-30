package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.SkuMappingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * SKU对照表管理Feign
 *
 * @author Jim
 * @date 2023/11/2
 */
@RestController
@RequestMapping("feign/listing")
public class ListingInfoFeignController extends BaseController {

    @Resource
    private ListingInfoService listingInfoService;

    @Resource
    private SkuMappingService skuMappingService;



    /**
     * 根据产品sku查询库存sku
     * @Author Luo_WG
     * @Date 2023/11/2 17:24
     * @param productSkuIdList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.listStockSkuNoByProductSkuNoView>
     **/
    @PostMapping("/listStockSkuNoByProductSkuIds")
    public List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> listStockSkuNoByProductSkuIds(@RequestBody List<String> productSkuIdList) {
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> list = skuMappingService.listStockSkuNoByProductSkuIds(productSkuIdList);
        return list;
    }

    /**
     * sku映射
     * @Author Luo_WG
     * @Date 2023/11/2 11:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/skuMapping")
    public Boolean skuMapping(@RequestBody @Validated FbaShipmentDTO.SkuMappingParamDTO dto) {
        return listingInfoService.skuMapping(dto);
    }

    /**
     * 检查和更新FnSku
     **/
    @PostMapping("/checkAndUpdateFnsku")
    public Boolean checkAndUpdateFnsku(@RequestBody @Validated ListingInfoParamDTO dto) {
        return listingInfoService.checkAndUpdateFnsku(dto);
    }

}
