package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.server.wms.query.VirtualInventoryDiffQueryHandler;
import com.erp.server.wms.service.VirtualInventoryDiffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 虚拟库存差异表
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@RestController
@LogSystemModule("虚拟库存差异表")
@RequestMapping("/virtualInventoryDiff")
public class VirtualInventoryDiffController extends BaseController {

    @Resource
    private VirtualInventoryDiffService virtualInventoryDiffService;

   /**
    * 库存差异列表
    * @author will
    * @date 2024/6/3 16:27
    * @param dto
    * @return ApiResult<PagingVO<ListDTO>>
    */
    @PostMapping("/diffPaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "diff.warehouseId",
            menuCode = "wms:virtualInventoryDiff:diffPaging"
    )
    @WebAdvanceQuery(handler = VirtualInventoryDiffQueryHandler.class)
    public ApiResult<PagingVO<VirtualInventoryDiffDTO.ListDTO>> diffPaging(@RequestBody @Validated PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        return success(virtualInventoryDiffService.diffPaging(dto));
    }

    /**
     * 库存差异数量
     * @author will
     * @date 2024/6/11 10:03
     * @param dto
     * @return ApiResult<Integer>
     */
    @PostMapping("/diffPagingCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "vi.warehouse_id",
            menuCode = "wms:virtualInventoryDiff:diffPaging"
    )
    public ApiResult<Integer> diffPagingCount(@RequestBody PermissionsDTO dto) {
        Integer count = virtualInventoryDiffService.diffPagingCount(dto);
        return success(count);
    }

    /**
     * 库存差异明细列表
     * @author will
     * @date 2024/6/3 16:45
     * @param dto
     * @return ApiResult<PagingVO<ListDetailDTO>>
     */
    @PostMapping("/diffDetailPaging")
    public ApiResult<PagingVO<VirtualInventoryDiffDTO.ListDetailQtyDTO>> diffDetailPaging(@RequestBody @Validated PagingDTO<VirtualInventoryDiffDTO.SearchParamDetailDTO> dto) {
        return success(virtualInventoryDiffService.diffDetailPaging(dto));
    }


    /**
     * 库存差异列表导出
     * @author will
     * @date 2024/6/3 17:57
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "库存差异列表导出")
    public ApiResult exportExcel(@RequestBody VirtualInventoryDiffDTO.SearchParamDTO dto) {
        Boolean flag = virtualInventoryDiffService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 一键调整查询
     * @author will
     * @date 2024/7/17 16:21
     * @param dto 
     * @return ApiResult<List<ListDetailQtyDTO>>
     */
    @PostMapping("/listDiffDetail")
    public ApiResult<List<VirtualInventoryDiffDTO.ListDetailQtyDTO>> listDiffDetail(@RequestBody @Validated VirtualInventoryDiffDTO.SearchParamDetailDTO dto) {
        return success(virtualInventoryDiffService.listDiffDetail(dto));
    }

    /**
     * 一键调整保存
     * @author will
     * @date 2024/7/17 18:17
     * @param list
     * @return ApiResult
     */
    @PostMapping("/updateVirtualInventory")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "一键调整保存")
    public ApiResult updateVirtualInventory(@RequestBody @Validated List<VirtualInventoryDiffDTO.UpdateVirtualInventoryDTO> list) {
        virtualInventoryDiffService.updateVirtualInventory(list);
        return success();
    }

    /**
     * 获取推荐数量
     * @author will
     * @date 2024/7/17 19:00
     * @param list
     * @return ApiResult<List<ListSuggestQtyDTO>>
     */
    @PostMapping("/listSuggestQty")
    public ApiResult<List<VirtualInventoryDiffDTO.ListSuggestQtyDTO>> listSuggestQty(@RequestBody @Validated List<VirtualInventoryDiffDTO.ListSuggestQtyParamDTO> list) {
        List<VirtualInventoryDiffDTO.ListSuggestQtyDTO> resultList = virtualInventoryDiffService.listSuggestQty(list);
        return success(resultList);
    }
}
