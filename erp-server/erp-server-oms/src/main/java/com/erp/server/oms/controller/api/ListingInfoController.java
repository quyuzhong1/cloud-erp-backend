package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.server.oms.query.ListingInfoQueryHandler;
import com.erp.server.oms.service.ListingInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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
     * 平台sku映射
     * @Author Luo_WG
     * @Date 2023/11/2 11:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/skuMapping")
    public ApiResult<Object> skuMapping(@RequestBody @Validated FbaShipmentDTO.SkuMappingParamDTO dto) {
        Boolean flag = listingInfoService.skuMapping(dto);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }


    /**
     * 库存sku映射
     **/
    @PostMapping("/warehouseSkuMapping")
    public ApiResult<Object> warehouseSkuMapping(@RequestBody @Validated ListingInfoDTO.WarehouseSkuMappingParamDTO dto) {
        Boolean flag = listingInfoService.warehouseSkuMapping(dto);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }

    /**
     * sku映射选择框列表
     * @Author Jim
     * @Date 2023/11/20
     **/
    @PostMapping("/select/list")
    public ApiResult<List<ListingInfoDTO.BaseDropDownDTO>> listByType(@RequestBody @Valid ListingInfoDTO.BaseDropDownParamDTO dto) {
        List<ListingInfoDTO.BaseDropDownDTO> list = listingInfoService.listByTypeWithFieldName(dto);
        return success(list);
    }

    /**
     * listing 分页
     **/
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = ListingInfoQueryHandler.class)
    public ApiResult<PagingVO<ListingInfoDTO.PageDTO>> paging(@RequestBody @Validated PagingDTO<ListingInfoDTO.PagingParamDTO> dto) {
        PagingVO<ListingInfoDTO.PageDTO> list = listingInfoService.paging(dto);
        return success(list);
    }
}
