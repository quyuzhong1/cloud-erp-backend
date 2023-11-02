package com.erp.server.oms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.server.oms.service.ListingInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import java.util.List;

/**
 * SKU对照表管理
 *
 * @author Lambda
 * @since 2023-08-18
 */
@RestController
@RequestMapping("/listing")
public class ListingInfoController extends BaseController {

    @Resource
    private ListingInfoService listingInfoService;


    /**
     * 根据类型获取对应 sku
     * @param type
     * @return
     */
    @GetMapping("list")
    public ApiResult<List<ListingInfoDTO.ListDTO>> listByType(@RequestParam("type") String type) {
        List<ListingInfoDTO.ListDTO> list = listingInfoService.listByType(type);
        return success(list);
    }


    /**
     * sku映射
     * @Author Luo_WG
     * @Date 2023/11/2 11:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/skuMapping")
    public ApiResult skuMapping(@RequestBody @Validated FbaShipmentDTO.skuMappingParamDTO dto) {
        Boolean flag = listingInfoService.skuMapping(dto);
        return flag ? success() : failure();
    }

}
