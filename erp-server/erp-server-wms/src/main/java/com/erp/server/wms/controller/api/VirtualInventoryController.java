package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.server.wms.service.VirtualInventoryService;
import com.erp.server.wms.service.VirtualTransFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 虚拟库存表
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@RestController
@LogSystemModule("虚拟库存表")
@RequestMapping("/virtualInventory")
public class VirtualInventoryController extends BaseController {

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Resource
    private VirtualTransFlowService virtualTransFlowService;

   /**
    * 虚拟库存列表
    * @author will
    * @date 2024/6/3 14:48
    * @param dto ApiResult<PagingVO<ListDTO>>
    */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<VirtualInventoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto) {
        return success(virtualInventoryService.paging(dto));
    }

    /**
     * 虚拟库存明细列表
     * @author will
     * @date 2024/6/3 16:55
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/detailPaging")
    public ApiResult<PagingVO<VirtualTransFlowDTO.InventoryDetailDTO>> detailPaging(@RequestBody @Validated PagingDTO<VirtualTransFlowDTO.InventoryDetailParamDTO> dto) {
        return success(virtualTransFlowService.detailPaging(dto));
    }

    /**
     * 虚拟仓库导出
     * @author will
     * @date 2024/6/3 17:14
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/exportExcel")
    @WebAdvanceQuery
    public ApiResult exportExcel(@RequestBody VirtualInventoryDTO.SearchParamDTO dto) {
        Boolean flag = virtualInventoryService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 根据条件查询库存信息
     *
     * @param qtyTypeDTO
     * @author hyj
     * @date 2024/6/6
     */
    @PostMapping(value = "/getQty")
    public ApiResult<List<VirtualInventoryDTO.ViewQtyDTO>> getQty(@RequestBody @Validated VirtualInventoryDTO.QtyTypeDTO qtyTypeDTO) {
        return success(virtualInventoryService.getQty(qtyTypeDTO));
    }

    /**
     * 查询bom虚拟库存信息
     * @author will
     * @date 2024/8/6 15:22
     * @param paramDTO
     * @return ApiResult<List<BomReturnDTO>>
     */
    @PostMapping(value = "/listBomVirtual")
    public ApiResult<List<VirtualInventoryDTO.BomReturnDTO>> listBomVirtual(@RequestBody @Validated VirtualInventoryDTO.BomParamDTO paramDTO) {
        return success(virtualInventoryService.listBomVirtual(paramDTO));
    }

    /**
     * SKU可用库存
     * @author will
     * @date 2024/8/8 18:26
     * @param paramList
     * @return ApiResult<SkuReturnDTO>
     */
    @PostMapping(value = "/listSkuVirtualInventoryQty")
    public ApiResult<List<VirtualInventoryDTO.SkuReturnDTO>> listSkuVirtualInventoryQty(@RequestBody @Validated List<VirtualInventoryDTO.BomParamDTO> paramList) {
        return success(virtualInventoryService.listSkuVirtualInventoryQty(paramList));
    }


}
